package com.passportscanner.models

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

data class PassportData(
    val fullName: String,
    val dateOfBirth: String,
    val passportNumber: String,
    val countryOfBirth: String
) : Serializable

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) 
    val id: Int = 0,
    val fullName: String,
    val dateOfBirth: String,
    val passportNumber: String,
    val countryOfBirth: String,
    val pdfPath: String,
    val timestamp: Long = System.currentTimeMillis()
)
