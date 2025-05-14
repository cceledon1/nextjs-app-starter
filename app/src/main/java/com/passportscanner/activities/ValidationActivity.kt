package com.passportscanner.activities

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.passportscanner.R
import com.passportscanner.database.AppDatabase
import com.passportscanner.database.DatabaseRepository
import com.passportscanner.models.PassportData
import com.passportscanner.models.ScanHistory
import com.passportscanner.utils.PDFGenerator
import kotlinx.coroutines.launch
import java.io.File

class ValidationActivity : AppCompatActivity() {
    private lateinit var fullNameLayout: TextInputLayout
    private lateinit var fullNameEdit: TextInputEditText
    private lateinit var dateOfBirthLayout: TextInputLayout
    private lateinit var dateOfBirthEdit: TextInputEditText
    private lateinit var passportNumberLayout: TextInputLayout
    private lateinit var passportNumberEdit: TextInputEditText
    private lateinit var countryOfBirthLayout: TextInputLayout
    private lateinit var countryOfBirthEdit: TextInputEditText
    private lateinit var generateButton: Button
    private lateinit var rescanButton: Button

    private lateinit var databaseRepository: DatabaseRepository
    private lateinit var pdfGenerator: PDFGenerator

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_validation)

        // Initialize views
        initializeViews()
        
        // Initialize database and PDF generator
        val database = AppDatabase.getDatabase(applicationContext)
        databaseRepository = DatabaseRepository(database.scanHistoryDao())
        pdfGenerator = PDFGenerator()

        // Get passport data from intent
        val passportData = intent.getSerializableExtra("passport_data") as PassportData
        
        // Populate fields with scanned data
        populateFields(passportData)

        // Set up button listeners
        setupButtonListeners()
    }

    private fun initializeViews() {
        fullNameLayout = findViewById(R.id.fullNameLayout)
        fullNameEdit = findViewById(R.id.fullNameEdit)
        dateOfBirthLayout = findViewById(R.id.dateOfBirthLayout)
        dateOfBirthEdit = findViewById(R.id.dateOfBirthEdit)
        passportNumberLayout = findViewById(R.id.passportNumberLayout)
        passportNumberEdit = findViewById(R.id.passportNumberEdit)
        countryOfBirthLayout = findViewById(R.id.countryOfBirthLayout)
        countryOfBirthEdit = findViewById(R.id.countryOfBirthEdit)
        generateButton = findViewById(R.id.generateButton)
        rescanButton = findViewById(R.id.rescanButton)
    }

    private fun populateFields(passportData: PassportData) {
        fullNameEdit.setText(passportData.fullName)
        dateOfBirthEdit.setText(passportData.dateOfBirth)
        passportNumberEdit.setText(passportData.passportNumber)
        countryOfBirthEdit.setText(passportData.countryOfBirth)
    }

    private fun setupButtonListeners() {
        generateButton.setOnClickListener {
            if (validateFields()) {
                generateInvitation()
            }
        }

        rescanButton.setOnClickListener {
            finish() // Return to scanner activity
        }
    }

    private fun validateFields(): Boolean {
        var isValid = true

        if (fullNameEdit.text.isNullOrBlank()) {
            fullNameLayout.error = "Full name is required"
            isValid = false
        } else {
            fullNameLayout.error = null
        }

        if (passportNumberEdit.text.isNullOrBlank()) {
            passportNumberLayout.error = "Passport number is required"
            isValid = false
        } else {
            passportNumberLayout.error = null
        }

        // Optional fields don't need validation
        dateOfBirthLayout.error = null
        countryOfBirthLayout.error = null

        return isValid
    }

    private fun generateInvitation() {
        val passportData = PassportData(
            fullName = fullNameEdit.text.toString(),
            dateOfBirth = dateOfBirthEdit.text.toString(),
            passportNumber = passportNumberEdit.text.toString(),
            countryOfBirth = countryOfBirthEdit.text.toString()
        )

        lifecycleScope.launch {
            try {
                // Generate PDF
                val pdfFile = pdfGenerator.generateInvitation(this@ValidationActivity, passportData)

                // Save to database
                val scanHistory = ScanHistory(
                    fullName = passportData.fullName,
                    dateOfBirth = passportData.dateOfBirth,
                    passportNumber = passportData.passportNumber,
                    countryOfBirth = passportData.countryOfBirth,
                    pdfPath = pdfFile.absolutePath
                )
                databaseRepository.insert(scanHistory)

                // Navigate to preview/share activity
                val intent = Intent(this@ValidationActivity, PreviewActivity::class.java).apply {
                    putExtra("pdf_path", pdfFile.absolutePath)
                }
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@ValidationActivity,
                    "Error generating invitation: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
