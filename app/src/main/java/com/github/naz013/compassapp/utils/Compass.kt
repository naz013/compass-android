package com.github.naz013.compassapp.utils

import android.app.Activity
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
import timber.log.Timber

class Compass(private val context: Context, compassListener: CompassListener) : SensorEventListener {

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

    // Listener
    private var mCompassListener: CompassListener
    // The minimum difference in degrees with the last orientation value for the CompassListener to be notified
    private var mAzimuthSensibility: Float = 0.toFloat()
    private var mPitchSensibility: Float = 0.toFloat()
    private var mRollSensibility: Float = 0.toFloat()
    // The last orientation value sent to the CompassListener
    private var mLastAzimuthDegrees: Float = 0.toFloat()
    private var mLastPitchDegrees: Float = 0.toFloat()
    private var mLastRollDegrees: Float = 0.toFloat()

    /**
     * Interface definition for [Compass] callbacks.
     */
    interface CompassListener {
        /**
         * Called whenever the device orientation has changed, providing azimuth, pitch and roll values taking into account the screen orientation of the device.
         * @param azimuth the azimuth of the device (East of the magnetic North = counterclockwise), in degrees, from 0° to 360°.
         * @param pitch the pitch (vertical inclination) of the device, in degrees, from -180° to 180°.<br></br>
         * Angle of rotation about the x axis. This value represents the angle between a plane parallel to the device's screen and a plane parallel to the ground.<br></br>
         * Equals 0° if the device top and bottom edges are on the same level.<br></br>
         * Equals -90° if the device top edge is up and the device bottom edge is down (such as when holding the device to take a picture towards the horizon).<br></br>
         * Equals 90° if the device top edge is down and the device bottom edge is up.
         * @param roll the roll (horizontal inclination) of the device, in degrees, from -90° to 90°.<br></br>
         * Angle of rotation about the y axis. This value represents the angle between a plane perpendicular to the device's screen and a plane perpendicular to the ground.<br></br>
         * Equals 0° if the device left and right edges are on the same level.<br></br>
         * Equals -90° if the device right edge is up and the device left edge is down.<br></br>
         * Equals 90° if the device right edge is down and the device left edge is up.
         */
        fun onOrientationChanged(azimuth: Float, pitch: Float, roll: Float)
    }

    init {
        // Sensors
        mMagnetometerSensor = mSensorManager.getDefaultSensor(TYPE_MAGNETIC_FIELD)
        mAccelerometerSensor = mSensorManager.getDefaultSensor(TYPE_ACCELEROMETER)
        mRotationVectorSensor = mSensorManager.getDefaultSensor(TYPE_ROTATION_VECTOR)

        // Listener
        mCompassListener = compassListener
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

    /**
     * Starts the [Compass].
     * Must be called in [Activity.onResume].
     * @param azimuthSensibility the minimum difference in degrees with the last azimuth measure for the [CompassListener] to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     * @param pitchSensibility the minimum difference in degrees with the last pitch measure for the [CompassListener] to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     * @param rollSensibility the minimum difference in degrees with the last roll measure for the [CompassListener] to be notified. Set to 0 (default value) to be notified of the slightest change, set to 360 to never be notified.
     */
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

    /**
     * Starts the [Compass] with default sensibility values.
     * Must be called in [Activity.onResume].
     */
    fun start() {
        start(0f, 0f, 0f)
    }

    /**
     * Stops the [Compass].
     * Must be called in [Activity.onPause].
     */
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
                mCompassListener.onOrientationChanged(mAzimuthDegrees, mPitchDegrees, mRollDegrees)
            }
        }
    }


    // SensorEventListener
    override fun onAccuracyChanged(sensor: Sensor, accuracy: Int) {
        // Nothing to do
    }

    /**
     * Exponential smoothing of data series, acting as a low-pass filter in order to remove high-frequency noise.
     * @param newValue the new data set.
     * @param lastValue the last data set.
     * @param alpha the smoothing factor. 0 < alpha < 1. If alpha = 0, the data will never change (lastValue = newValue). If alpha = 1, no smoothing at all will be applied (lastValue = newValue).
     * @return output the new data entry, smoothened.
     */
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

        /**
         * Factory method that returns a new [Compass] instance.
         * @param context the current [Context].
         * @param compassListener the listener for this [Compass] events.
         * @return a new [Compass] instance or **null** if the device does not have the required sensors.
         */
        @Nullable
        fun newInstance(context: Context, compassListener: CompassListener): Compass? {
            val compass = Compass(context, compassListener)
            return if (compass.hasRequiredSensors()) {
                compass
            } else {
                null
            }
        }
    }
}