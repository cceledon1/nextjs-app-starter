package com.passportscanner.utils

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.passportscanner.models.PassportData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.regex.Pattern

class OCRProcessor {
    private val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    suspend fun processImage(bitmap: Bitmap): PassportData? {
        return withContext(Dispatchers.Default) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()
                parsePassportData(result.text)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            }
        }
    }

    private fun parsePassportData(text: String): PassportData? {
        try {
            // MRZ typically consists of 2 or 3 lines of 44 characters each
            val lines = text.split("\n")
                .map { it.trim() }
                .filter { it.length >= 30 } // Filter potential MRZ lines

            if (lines.isEmpty()) return null

            // Try to find passport number using common patterns
            val passportNumberPattern = Pattern.compile("[A-Z][0-9]{8}")
            var passportNumber = ""
            var fullName = ""
            var dateOfBirth = ""
            var countryOfBirth = ""

            // Search for passport number
            for (line in lines) {
                val matcher = passportNumberPattern.matcher(line)
                if (matcher.find()) {
                    passportNumber = matcher.group()
                    break
                }
            }

            // Look for name (usually in capital letters)
            val namePattern = Pattern.compile("[A-Z]{2,}(?:\\s[A-Z]{2,})+")
            for (line in lines) {
                val matcher = namePattern.matcher(line)
                if (matcher.find()) {
                    fullName = matcher.group().trim()
                    break
                }
            }

            // Look for date of birth (common formats)
            val dobPattern = Pattern.compile("\\d{2}(?:0[1-9]|1[0-2])(?:0[1-9]|[12]\\d|3[01])")
            for (line in lines) {
                val matcher = dobPattern.matcher(line)
                if (matcher.find()) {
                    val dob = matcher.group()
                    dateOfBirth = "${dob.substring(0,2)}/${dob.substring(2,4)}/${dob.substring(4)}"
                    break
                }
            }

            // Look for country code (3 letter country codes)
            val countryPattern = Pattern.compile("[A-Z]{3}")
            for (line in lines) {
                val matcher = countryPattern.matcher(line)
                if (matcher.find()) {
                    countryOfBirth = matcher.group()
                    break
                }
            }

            // Return null if we couldn't find essential information
            if (passportNumber.isEmpty() || fullName.isEmpty()) {
                return null
            }

            return PassportData(
                fullName = fullName,
                dateOfBirth = dateOfBirth,
                passportNumber = passportNumber,
                countryOfBirth = countryOfBirth
            )
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }

    companion object {
        private const val TAG = "OCRProcessor"
    }
}
