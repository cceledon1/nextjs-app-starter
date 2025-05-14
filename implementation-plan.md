# Passport Scanner & Invitation Generator - Implementation Plan

## 1. Project Structure

```
app/
├── java/
│   └── com.passportscanner/
│       ├── activities/
│       │   ├── MainActivity.kt
│       │   ├── ScannerActivity.kt
│       │   ├── ValidationActivity.kt
│       │   └── HistoryActivity.kt
│       ├── models/
│       │   ├── PassportData.kt
│       │   └── ScanHistory.kt
│       ├── utils/
│       │   ├── OCRProcessor.kt
│       │   ├── PDFGenerator.kt
│       │   └── DatabaseHelper.kt
│       └── views/
│           └── CustomViews.kt
└── res/
    ├── layout/
    │   ├── activity_main.xml
    │   ├── activity_scanner.xml
    │   ├── activity_validation.xml
    │   └── activity_history.xml
    └── values/
        ├── strings.xml
        ├── colors.xml
        └── styles.xml
```

## 2. Dependencies (build.gradle)

```gradle
dependencies {
    // ML Kit for OCR and MRZ
    implementation 'com.google.mlkit:text-recognition:16.0.0'
    
    // Camera X
    implementation 'androidx.camera:camera-camera2:1.1.0'
    implementation 'androidx.camera:camera-lifecycle:1.1.0'
    implementation 'androidx.camera:camera-view:1.1.0'
    
    // PDF Generation
    implementation 'com.itextpdf:itext7-core:7.2.3'
    
    // Room Database
    implementation 'androidx.room:room-runtime:2.4.3'
    kapt 'androidx.room:room-compiler:2.4.3'
    
    // Coroutines
    implementation 'org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4'
    
    // ViewModels and LiveData
    implementation 'androidx.lifecycle:lifecycle-viewmodel-ktx:2.5.1'
    implementation 'androidx.lifecycle:lifecycle-livedata-ktx:2.5.1'
}
```

## 3. Implementation Steps

### 3.1 Data Models

#### PassportData.kt
```kotlin
data class PassportData(
    val fullName: String,
    val dateOfBirth: String,
    val passportNumber: String,
    val countryOfBirth: String
)

@Entity(tableName = "scan_history")
data class ScanHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val passportData: PassportData,
    val pdfPath: String,
    val timestamp: Long = System.currentTimeMillis()
)
```

### 3.2 OCR Implementation

#### OCRProcessor.kt
```kotlin
class OCRProcessor {
    private val recognizer = TextRecognition.getClient()
    
    suspend fun processImage(bitmap: Bitmap): PassportData? {
        return withContext(Dispatchers.Default) {
            try {
                val image = InputImage.fromBitmap(bitmap, 0)
                val result = recognizer.process(image).await()
                parsePassportData(result)
            } catch (e: Exception) {
                null
            }
        }
    }
    
    private fun parsePassportData(result: Text): PassportData? {
        // Implement MRZ parsing logic
        // Return PassportData object if successful
    }
}
```

### 3.3 PDF Generation

#### PDFGenerator.kt
```kotlin
class PDFGenerator {
    fun generateInvitation(context: Context, passportData: PassportData): File {
        val pdfDocument = Document()
        val file = File(context.filesDir, "invitation_${System.currentTimeMillis()}.pdf")
        
        PdfWriter.getInstance(pdfDocument, FileOutputStream(file))
        pdfDocument.open()
        
        // Add content
        pdfDocument.add(Paragraph("Слава Україні!"))
        pdfDocument.add(Paragraph("Hello!"))
        pdfDocument.add(Paragraph("We want to announce that, ${passportData.fullName}, " +
            "N° ${passportData.passportNumber}, has been selected and approved for service..."))
        
        pdfDocument.close()
        return file
    }
}
```

### 3.4 Database Implementation

#### AppDatabase.kt
```kotlin
@Database(entities = [ScanHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanHistoryDao(): ScanHistoryDao
}

@Dao
interface ScanHistoryDao {
    @Query("SELECT * FROM scan_history ORDER BY timestamp DESC")
    fun getAllScans(): Flow<List<ScanHistory>>
    
    @Insert
    suspend fun insertScan(scan: ScanHistory)
    
    @Delete
    suspend fun deleteScan(scan: ScanHistory)
}
```

### 3.5 UI Implementation

#### activity_scanner.xml
```xml
<?xml version="1.0" encoding="utf-8"?>
<androidx.constraintlayout.widget.ConstraintLayout 
    xmlns:android="http://schemas.android.com/apk/res/android"
    android:layout_width="match_parent"
    android:layout_height="match_parent">
    
    <androidx.camera.view.PreviewView
        android:id="@+id/viewFinder"
        android:layout_width="match_parent"
        android:layout_height="match_parent" />
    
    <com.google.android.material.floatingactionbutton.FloatingActionButton
        android:id="@+id/captureButton"
        android:layout_width="wrap_content"
        android:layout_height="wrap_content"
        android:layout_margin="16dp"
        android:src="@drawable/ic_camera" />
        
</androidx.constraintlayout.widget.ConstraintLayout>
```

#### ScannerActivity.kt
```kotlin
class ScannerActivity : AppCompatActivity() {
    private lateinit var cameraProvider: ProcessCameraProvider
    private lateinit var ocrProcessor: OCRProcessor
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_scanner)
        
        requestCameraPermission()
        setupCamera()
        
        captureButton.setOnClickListener {
            takePhoto()
        }
    }
    
    private fun takePhoto() {
        // Implement photo capture and OCR processing
        // Navigate to ValidationActivity with results
    }
}
```

## 4. Features Implementation Order

1. Camera & OCR Integration
   - Set up CameraX
   - Implement OCR processing
   - Basic UI for camera preview

2. Data Validation
   - Create validation form
   - Implement manual editing
   - Data validation rules

3. PDF Generation
   - Template creation
   - Dynamic data insertion
   - File saving

4. History Management
   - Database setup
   - History UI
   - CRUD operations

5. Sharing & Export
   - PDF sharing implementation
   - Multiple export options
   - File management

## 5. Testing Strategy

1. Unit Tests
   - OCR parsing logic
   - PDF generation
   - Database operations

2. Integration Tests
   - Camera and OCR integration
   - PDF generation with real data
   - Database interactions

3. UI Tests
   - Camera preview
   - Form validation
   - Navigation flow

## 6. Security Considerations

1. Data Storage
   - Encrypt sensitive data
   - Secure file storage
   - Regular cleanup of temporary files

2. Permissions
   - Camera permission handling
   - Storage permission handling
   - Runtime permission checks

3. PDF Security
   - Secure PDF generation
   - Temporary file handling
   - Clean up after sharing
