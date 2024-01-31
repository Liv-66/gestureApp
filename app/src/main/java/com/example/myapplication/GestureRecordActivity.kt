package com.example.myapplication

import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.media.MediaRecorder
import android.os.CountDownTimer
import android.util.Log
import androidx.core.app.ActivityCompat
import java.io.File
import android.Manifest
import android.content.Context
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.os.Handler
import android.view.Surface
import android.view.TextureView


class GestureRecordActivity<IOException : Any> : AppCompatActivity() {
    @SuppressLint("MissingInflatedId")
    // Define a variable to track recording state
    private var isRecording = false
    private lateinit var mediaRecorder: MediaRecorder
    private lateinit var selectedGesture: String
    private var practiceNumber = 1
    private lateinit var outputFilePath: String
    private lateinit var cameraManager: CameraManager
    private lateinit var cameraDevice: CameraDevice

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_gesture_record)

        selectedGesture = intent.getStringExtra("SELECTED_GESTURE").toString()

        // Set up MediaRecorder
        openCamera()
        setupMediaRecorder()

        val recordButton: Button = findViewById(R.id.record_button)
        recordButton.setOnClickListener {
            startRecording()
        }

        val uploadButton: Button = findViewById(R.id.upload_button)
        uploadButton.setOnClickListener {
            uploadVideo()
        }
    }

    private fun openCamera() {
        val cameraManager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            // Declare stateCallback and backgroundHandler as class members
            val stateCallback: CameraDevice.StateCallback
            val backgroundHandler: Handler

            // Inside onCreate or a setup function:
            stateCallback = object : CameraDevice.StateCallback() {
                override fun onOpened(camera: CameraDevice) {
                    cameraDevice = camera
                    val captureRequest = camera.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
                    val textureView: TextureView = findViewById(R.id.camera_preview)
                    val surfaceTexture = textureView.surfaceTexture // Get SurfaceTexture from the TextureView
                    val previewSurface = Surface(surfaceTexture) // Create a Surface for the camera preview

                    captureRequest.addTarget(previewSurface)

                    val captureRequestBuilt = captureRequest.build()

                    camera.createCaptureSession(listOf(previewSurface), object : CameraCaptureSession.StateCallback() {
                        override fun onConfigured(session: CameraCaptureSession) {
                            // The camera is already closed
                            if (cameraDevice == null) return

                            try {
                                // Start displaying the camera preview.
                                session.setRepeatingRequest(captureRequestBuilt, null, null)
                            } catch (e: CameraAccessException) {
                                Log.e("GestureRecordActivity", "createCaptureSession", e)
                            }
                        }
                        override fun onConfigureFailed(session: CameraCaptureSession) {
                            Log.e("GestureRecordActivity", "Configuration failed")
                        }
                    }, null)
                }

                override fun onDisconnected(camera: CameraDevice) {
                    cameraDevice.close()
                }

                override fun onError(camera: CameraDevice, error: Int) {
                    cameraDevice.close()
                }
            }

            val cameraId = getFrontFacingCameraId()

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10);
            }
            cameraManager.openCamera(cameraId!!, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getFrontFacingCameraId(): String? {
        try {
            cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager
            for (cameraId in cameraManager.cameraIdList) {
                val characteristics = cameraManager.getCameraCharacteristics(cameraId)
                val facing = characteristics.get(CameraCharacteristics.LENS_FACING)
                if (facing != null && facing == CameraCharacteristics.LENS_FACING_FRONT) {
                    return cameraId
                }
            }
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
        return null
    }

    private fun getFilenameFriendlyGestureName(gestureName: String?): String {
        return when (gestureName) {
            "Turn on lights" -> "LightOn"
            "Turn off lights" -> "LightOff"
            "Turn on fan" -> "FanOn"
            "Turn off fan" -> "FanOff"
            "Increase fan speed" -> "FanUp"
            "Decrease fan speed" -> "FanDown"
            "Set Thermostat to specified temperature" -> "SetThermo"
            "Gesture 0" -> "Num0"
            "Gesture 1" -> "Num1"
            "Gesture 2" -> "Num2"
            "Gesture 3" -> "Num3"
            "Gesture 4" -> "Num4"
            "Gesture 5" -> "Num5"
            "Gesture 6" -> "Num6"
            "Gesture 7" -> "Num7"
            "Gesture 8" -> "Num8"
            "Gesture 9" -> "Num9"
            else -> "Unknown"
        }
    }

    private fun setupMediaRecorder() {
        mediaRecorder = MediaRecorder()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), 10);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.RECORD_AUDIO), 10);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), 10);
        }
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), 10);
        }

        val fileName = getVideoFileName()
        val internalStorageDir = cacheDir
        val videoFile = File(internalStorageDir, fileName)
        outputFilePath = videoFile.absolutePath
        Log.d("GestureRecordActivity", "outputFilePath: $outputFilePath")

        // Configure sources
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.SURFACE)
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.DEFAULT)
        mediaRecorder.setOutputFile(outputFilePath)

        // Prepare the MediaRecorder
        try {
            mediaRecorder.prepare()
        } catch (e: Throwable) {
            Log.e("GestureRecordActivity", "MediaRecorder prepare failed", e)
        }
    }

    private fun getVideoFileName(): String {
        val gestureLabel = getFilenameFriendlyGestureName(selectedGesture)
        val userLastName = "HUANG"
        return "${gestureLabel}_PRACTICE_${practiceNumber.toString()}_${userLastName}"
    }

    private val recordingTimer: CountDownTimer = object : CountDownTimer(5000, 1000) {
        override fun onTick(millisUntilFinished: Long) {
            // Not used, but required for CountDownTimer
        }
        override fun onFinish() {
            try {
                mediaRecorder.stop()

            } catch (e: Throwable) {
                Log.e("GestureRecordActivity", "mediaRecorder stop failed", e)
            }

//            val videoFile = File(outputFilePath)
//            val videoUri = FileProvider.getUriForFile(
//                this@GestureRecordActivity,
//                "${applicationContext.packageName}.provider",
//                videoFile
//            )
//
//            Intent(Intent.ACTION_VIEW).also {
//                it.setDataAndType(videoUri, "video/mp4")
//                it.flags = Intent.FLAG_GRANT_READ_URI_PERMISSION
//                startActivity(it)
//            }
        }
    }

    private fun uploadVideo() {

    }

    private fun startRecording() {
        if (isRecording) {
            resetRecording()
        }
        mediaRecorder.start()
        isRecording = true

        // Stop recording after 5 seconds
        recordingTimer.start()
    }

    private fun resetRecording() {
        mediaRecorder.apply {
            stop()
            reset()
            release()
        }
        isRecording = false
        recordingTimer.cancel()
    }
}
