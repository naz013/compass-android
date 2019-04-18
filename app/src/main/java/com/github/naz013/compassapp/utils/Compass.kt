package com.github.naz013.compassapp.utils

import android.content.Context
import android.content.Context.SENSOR_SERVICE
import android.content.Context.WINDOW_SERVICE
import android.hardware.Sensor
import android.hardware.Sensor.*
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.view.Surface.*
import android.view.WindowManager
import androidx.annotation.Nullable
import androidx.lifecycle.MutableLiveData
import timber.log.Timber

class Compass(private val context: Context) : SensorEventListener {

    // Sensors
    private var mSensorManager: SensorManager = context.getSystemService(SENSOR_SERVICE) as SensorManager
    private var mRotationVectorSensor: Sensor?
    private var mMagnetometerSensor: Sensor?
    private var mAccelerometerSensor: Sensor?
    // RotationVectorSensor is more precise than Magnetic+Accelerometer, but on some devices it is not working
    private var mUseRotationVectorSensor = false

    // Orientation
    private var mAzimuthDegrees: Float = 0.toFloat()
    private var mPitchDegrees: Float = 0.toFloat()
    private var mRollDegrees: Float = 0.toFloat()
    private var mRotationVector = FloatArray(5)
    private var mGeomagnetic = FloatArray(3)
    private var mGravity = FloatArray(3)

    // The minimum difference in degrees with the last orientation value for the CompassListener to be notified
    private var mAzimuthSensibility: Float = 0.toFloat()
    private var mPitchSensibility: Float = 0.toFloat()
    private var mRollSensibility: Float = 0.toFloat()
    // The last orientation value sent to the CompassListener
    private var mLastAzimuthDegrees: Float = 0.toFloat()
    private var mLastPitchDegrees: Float = 0.toFloat()
    private var mLastRollDegrees: Float = 0.toFloat()

    val angle: MutableLiveData<Float> = MutableLiveData()

    init {
        // Sensors
        mMagnetometerSensor = mSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
        mAccelerometerSensor = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        mRotationVectorSensor = mSensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)
    }

    // Check that the device has the required sensors
    private fun hasRequiredSensors(): Boolean {
        return if (mRotationVectorSensor != null) {
            Timber.d("Sensor.TYPE_ROTATION_VECTOR found")
            true
        } else if (mMagnetometerSensor != null && mAccelerometerSensor != null) {
            Timber.d("Sensor.TYPE_MAGNETIC_FIELD and Sensor.TYPE_ACCELEROMETER found")
            true
        } else {
            Timber.d("The device does not have the required sensors")
            false
        }
    }

    fun start(azimuthSensibility: Float, pitchSensibility: Float, rollSensibility: Float) {
        mAzimuthSensibility = azimuthSensibility
        mPitchSensibility = pitchSensibility
        mRollSensibility = rollSensibility
        if (mRotationVectorSensor != null) {
            mSensorManager.registerListener(this, mRotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (mMagnetometerSensor != null) {
            mSensorManager.registerListener(this, mMagnetometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
        if (mAccelerometerSensor != null) {
            mSensorManager.registerListener(this, mAccelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL)
        }
    }

    fun start() {
        start(0f, 0f, 0f)
    }

    fun stop() {
        mAzimuthSensibility = 0f
        mPitchSensibility = 0f
        mRollSensibility = 0f
        mSensorManager.unregisterListener(this)
    }

    // SensorEventListener
    override fun onSensorChanged(event: SensorEvent) {
        synchronized(this) {
            // Get the orientation array with Sensor.TYPE_ROTATION_VECTOR if possible (more precise), otherwise with Sensor.TYPE_MAGNETIC_FIELD and Sensor.TYPE_ACCELEROMETER combined
            val orientation = FloatArray(3)
            if (event.sensor.type == TYPE_ROTATION_VECTOR) {
                // Only use rotation vector sensor if it is working on this device
                if (!mUseRotationVectorSensor) {
                    Timber.d("Using Sensor.TYPE_ROTATION_VECTOR (more precise compass data)")
                    mUseRotationVectorSensor = true
                }
                // Smooth values
                mRotationVector = exponentialSmoothing(event.values, mRotationVector,
                    ROTATION_VECTOR_SMOOTHING_FACTOR
                )
                // Calculate the rotation matrix
                val rotationMatrix = FloatArray(9)
                SensorManager.getRotationMatrixFromVector(rotationMatrix, event.values)
                // Calculate the orientation
                SensorManager.getOrientation(rotationMatrix, orientation)
            } else if (!mUseRotationVectorSensor && (event.sensor.type == TYPE_MAGNETIC_FIELD || event.sensor.type == TYPE_ACCELEROMETER)) {
                if (event.sensor.type == TYPE_MAGNETIC_FIELD) {
                    mGeomagnetic = exponentialSmoothing(event.values, mGeomagnetic,
                        GEOMAGNETIC_SMOOTHING_FACTOR
                    )
                }
                if (event.sensor.type == TYPE_ACCELEROMETER) {
                    mGravity = exponentialSmoothing(event.values, mGravity,
                        GRAVITY_SMOOTHING_FACTOR
                    )
                }
                // Calculate the rotation and inclination matrix
                val rotationMatrix = FloatArray(9)
                val inclinationMatrix = FloatArray(9)
                SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, mGravity, mGeomagnetic)
                // Calculate the orientation
                SensorManager.getOrientation(rotationMatrix, orientation)
            } else {
                return
            }

            // Calculate azimuth, pitch and roll values from the orientation[] array
            // Correct values depending on the screen rotation
            val screenRotation =
                (context.getSystemService(WINDOW_SERVICE) as WindowManager).defaultDisplay.rotation
            mAzimuthDegrees = Math.toDegrees(orientation[0].toDouble()).toFloat()
            if (screenRotation == ROTATION_0) {
                mPitchDegrees = Math.toDegrees(orientation[1].toDouble()).toFloat()
                mRollDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat()
                if (mRollDegrees >= 90 || mRollDegrees <= -90) {
                    mAzimuthDegrees += 180f
                    mPitchDegrees = if (mPitchDegrees > 0) 180 - mPitchDegrees else -180 - mPitchDegrees
                    mRollDegrees = if (mRollDegrees > 0) 180 - mRollDegrees else -180 - mRollDegrees
                }
            } else if (screenRotation == ROTATION_90) {
                mAzimuthDegrees += 90f
                mPitchDegrees = Math.toDegrees(orientation[2].toDouble()).toFloat()
                mRollDegrees = (-Math.toDegrees(orientation[1].toDouble())).toFloat()
            } else if (screenRotation == ROTATION_180) {
                mAzimuthDegrees += 180f
                mPitchDegrees = (-Math.toDegrees(orientation[1].toDouble())).toFloat()
                mRollDegrees = (-Math.toDegrees(orientation[2].toDouble())).toFloat()
                if (mRollDegrees >= 90 || mRollDegrees <= -90) {
                    mAzimuthDegrees += 180f
                    mPitchDegrees = if (mPitchDegrees > 0) 180 - mPitchDegrees else -180 - mPitchDegrees
                    mRollDegrees = if (mRollDegrees > 0) 180 - mRollDegrees else -180 - mRollDegrees
                }
            } else if (screenRotation == ROTATION_270) {
                mAzimuthDegrees += 270f
                mPitchDegrees = (-Math.toDegrees(orientation[2].toDouble())).toFloat()
                mRollDegrees = Math.toDegrees(orientation[1].toDouble()).toFloat()
            }

            // Force azimuth value between 0° and 360°.
            mAzimuthDegrees = (mAzimuthDegrees + 360) % 360

            // Notify the compass listener if needed
            if (Math.abs(mAzimuthDegrees - mLastAzimuthDegrees) >= mAzimuthSensibility
                || Math.abs(mPitchDegrees - mLastPitchDegrees) >= mPitchSensibility
                || Math.abs(mRollDegrees - mLastRollDegrees) >= mRollSensibility
                || mLastAzimuthDegrees == 0f
            ) {
                mLastAzimuthDegrees = mAzimuthDegrees
                mLastPitchDegrees = mPitchDegrees
                mLastRollDegrees = mRollDegrees
                angle.postValue(mAzimuthDegrees)
            }
        }
    }


    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
    }

    private fun exponentialSmoothing(newValue: FloatArray, lastValue: FloatArray?, alpha: Float): FloatArray {
        val output = FloatArray(newValue.size)
        if (lastValue == null) {
            return newValue
        }
        for (i in newValue.indices) {
            output[i] = lastValue[i] + alpha * (newValue[i] - lastValue[i])
        }
        return output
    }

    companion object {
        private const val ROTATION_VECTOR_SMOOTHING_FACTOR = 0.5f
        private const val GEOMAGNETIC_SMOOTHING_FACTOR = 0.4f
        private const val GRAVITY_SMOOTHING_FACTOR = 0.1f

        @Nullable
        fun newInstance(context: Context): Compass? {
            val compass = Compass(context)
            return if (compass.hasRequiredSensors()) {
                compass
            } else {
                null
            }
        }
    }
}