package com.passportscanner.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.passportscanner.R
import com.passportscanner.utils.OCRProcessor
import kotlinx.coroutines.launch
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.View
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class ScannerActivity : AppCompatActivity() {
    private lateinit var viewFinder: PreviewView
    private lateinit var captureButton: FloatingActionButton
    private lateinit var progressIndicator: CircularProgressIndicator
    private lateinit var cameraExecutor: ExecutorService
    private var imageCapture: ImageCapture? = null
    private val ocrProcessor = OCRProcessor()

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startCamera()
        } else {
            Toast.makeText(this, "Camera permission is required", Toast.LENGTH_LONG).show()
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)

        viewFinder = findViewById(R.id.viewFinder)
        captureButton = findViewById(R.id.captureButton)
        progressIndicator = findViewById(R.id.progressIndicator)

        cameraExecutor = Executors.newSingleThreadExecutor()

        if (hasPermissions()) {
            startCamera()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.CAMERA)
        }

        captureButton.setOnClickListener {
            takePhoto()
        }
    }

    private fun hasPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(
            this, Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(viewFinder.surfaceProvider)
                }

            imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MAXIMIZE_QUALITY)
                .build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (e: Exception) {
                Toast.makeText(this, "Failed to start camera", Toast.LENGTH_SHORT).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        progressIndicator.isVisible = true
        captureButton.isEnabled = false

        val photoFile = File(
            externalCacheDir,
            SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.getDefault())
                .format(System.currentTimeMillis()) + ".jpg"
        )

        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    processImage(photoFile)
                }

                override fun onError(exc: ImageCaptureException) {
                    progressIndicator.isVisible = false
                    captureButton.isEnabled = true
                    Toast.makeText(
                        this@ScannerActivity,
                        "Failed to capture image",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        )
    }

    private fun processImage(photoFile: File) {
        lifecycleScope.launch {
            try {
                val bitmap = BitmapFactory.decodeFile(photoFile.absolutePath)
                val passportData = ocrProcessor.processImage(bitmap)

                if (passportData != null) {
                    val intent = Intent(this@ScannerActivity, ValidationActivity::class.java).apply {
                        putExtra("passport_data", passportData)
                    }
                    startActivity(intent)
                } else {
                    Toast.makeText(
                        this@ScannerActivity,
                        "Could not extract passport data. Please try again.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            } catch (e: Exception) {
                Toast.makeText(
                    this@ScannerActivity,
                    "Error processing image: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                progressIndicator.isVisible = false
                captureButton.isEnabled = true
                photoFile.delete() // Clean up temporary file
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
    }
}
