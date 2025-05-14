package com.passportscanner.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.github.barteksc.pdfviewer.PDFView
import com.passportscanner.R
import java.io.File

class PreviewActivity : AppCompatActivity() {
    private lateinit var pdfView: PDFView
    private lateinit var shareButton: Button
    private lateinit var newScanButton: Button
    private lateinit var pdfFile: File

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_preview)

        // Initialize views
        pdfView = findViewById(R.id.pdfView)
        shareButton = findViewById(R.id.shareButton)
        newScanButton = findViewById(R.id.newScanButton)

        // Get PDF file path from intent
        val pdfPath = intent.getStringExtra("pdf_path")
        if (pdfPath == null) {
            Toast.makeText(this, "Error: PDF file not found", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        pdfFile = File(pdfPath)
        if (!pdfFile.exists()) {
            Toast.makeText(this, "Error: PDF file does not exist", Toast.LENGTH_LONG).show()
            finish()
            return
        }

        // Load PDF
        loadPdf()

        // Set up button listeners
        setupButtons()
    }

    private fun loadPdf() {
        try {
            pdfView.fromFile(pdfFile)
                .enableSwipe(true)
                .swipeHorizontal(false)
                .enableDoubletap(true)
                .defaultPage(0)
                .load()
        } catch (e: Exception) {
            Toast.makeText(this, "Error loading PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun setupButtons() {
        shareButton.setOnClickListener {
            sharePdf()
        }

        newScanButton.setOnClickListener {
            // Clear activity stack and start fresh scan
            val intent = Intent(this, ScannerActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            startActivity(intent)
        }
    }

    private fun sharePdf() {
        try {
            val uri = FileProvider.getUriForFile(
                this,
                "${applicationContext.packageName}.provider",
                pdfFile
            )

            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            val chooser = Intent.createChooser(shareIntent, "Share Invitation")
            startActivity(chooser)
        } catch (e: Exception) {
            Toast.makeText(this, "Error sharing PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // Clean up temporary files if needed
        // Note: You might want to keep the files for history feature
        // pdfFile.delete()
    }
}
