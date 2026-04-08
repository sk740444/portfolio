package com.securecam.ui.camera

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.PendingRecording
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.securecam.databinding.ActivityMainBinding
import com.securecam.domain.model.MediaType
import com.securecam.util.TimeProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val viewModel: CameraViewModel by viewModels()

    private var imageCapture: ImageCapture? = null
    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null
    private lateinit var cameraExecutor: ExecutorService

    private val permissionsLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
        if (hasAllPermissions()) startCamera() else Toast.makeText(this, "Permissions required", Toast.LENGTH_LONG).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        cameraExecutor = Executors.newSingleThreadExecutor()

        observeUi()
        setupActions()

        if (hasAllPermissions()) startCamera() else requestPermissions()
    }

    private fun observeUi() {
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { state ->
                binding.tvLocation.text = state.locationStatus
                binding.tvHash.text = state.hashStatus
                binding.tvReport.text = state.reportStatus
                binding.btnCapture.text = when (state.mode) {
                    CaptureMode.PHOTO -> "Capture Photo"
                    CaptureMode.VIDEO -> if (state.isRecording) "Stop Recording" else "Start Recording"
                }
                binding.btnToggleMode.text = if (state.mode == CaptureMode.PHOTO) "Switch to Video" else "Switch to Photo"
            }
        }
    }

    private fun setupActions() {
        binding.btnToggleMode.setOnClickListener {
            val next = if (viewModel.uiState.value.mode == CaptureMode.PHOTO) CaptureMode.VIDEO else CaptureMode.PHOTO
            viewModel.setMode(next)
        }

        binding.btnCapture.setOnClickListener {
            if (viewModel.uiState.value.mode == CaptureMode.PHOTO) takePhoto() else toggleVideo()
        }
    }

    private fun requestPermissions() {
        permissionsLauncher.launch(REQUIRED_PERMISSIONS)
    }

    private fun hasAllPermissions(): Boolean = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val provider = cameraProviderFuture.get()
            val preview = Preview.Builder().build().also {
                it.surfaceProvider = binding.previewView.surfaceProvider
            }
            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()
            val recorder = Recorder.Builder().setQualitySelector(QualitySelector.from(Quality.HIGHEST)).build()
            videoCapture = VideoCapture.withOutput(recorder)

            provider.unbindAll()
            provider.bindToLifecycle(this, CameraSelector.DEFAULT_BACK_CAMERA, preview, imageCapture, videoCapture)
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val capture = imageCapture ?: return
        lifecycleScope.launch {
            val location = viewModel.fetchLocation()
            val stamp = TimeProvider.nowFileStampUtc()
            val iso = TimeProvider.nowIsoUtc()
            val name = "IMG_$stamp"
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.jpg")
                put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Pictures/SecureCam")
            }
            val options = ImageCapture.OutputFileOptions.Builder(
                contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                values
            ).build()

            capture.takePicture(options, ContextCompat.getMainExecutor(this@MainActivity), object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: return
                    viewModel.processIntegrity(savedUri, "$name.jpg", "Pictures/SecureCam", MediaType.IMAGE, iso, location)
                }

                override fun onError(exception: ImageCaptureException) {
                    Toast.makeText(this@MainActivity, "Photo failed: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    private fun toggleVideo() {
        val active = recording
        if (active != null) {
            active.stop()
            recording = null
            viewModel.setRecording(false)
            return
        }

        lifecycleScope.launch {
            val location = viewModel.fetchLocation()
            val stamp = TimeProvider.nowFileStampUtc()
            val iso = TimeProvider.nowIsoUtc()
            val name = "VID_$stamp"
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, "$name.mp4")
                put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Movies/SecureCam")
            }
            val outputOptions = androidx.camera.video.MediaStoreOutputOptions.Builder(contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                .setContentValues(contentValues)
                .build()
            val pendingRecording: PendingRecording = videoCapture!!.output.prepareRecording(this@MainActivity, outputOptions)
                .withAudioEnabled()

            recording = pendingRecording.start(ContextCompat.getMainExecutor(this@MainActivity)) { event ->
                when (event) {
                    is VideoRecordEvent.Start -> viewModel.setRecording(true)
                    is VideoRecordEvent.Finalize -> {
                        viewModel.setRecording(false)
                        val uri = event.outputResults.outputUri
                        if (event.hasError()) {
                            Toast.makeText(this@MainActivity, "Video error: ${event.error}", Toast.LENGTH_SHORT).show()
                        } else {
                            viewModel.processIntegrity(uri, "$name.mp4", "Movies/SecureCam", MediaType.VIDEO, iso, location)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }

    companion object {
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.RECORD_AUDIO
        )
    }
}

