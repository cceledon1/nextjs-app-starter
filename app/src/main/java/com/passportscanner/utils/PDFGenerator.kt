package com.passportscanner.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import com.itextpdf.kernel.pdf.PdfDocument
import com.itextpdf.kernel.pdf.PdfWriter
import com.itextpdf.layout.Document
import com.itextpdf.layout.element.Paragraph
import com.itextpdf.layout.element.Text
import com.itextpdf.layout.property.TextAlignment
import com.passportscanner.models.PassportData
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

class PDFGenerator {
    fun generateInvitation(context: Context, passportData: PassportData): File {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "invitation_${timestamp}.pdf"
        val file = File(context.getExternalFilesDir(null), fileName)

        PdfWriter(file).use { writer ->
            val pdfDoc = PdfDocument(writer)
            Document(pdfDoc).use { document ->
                // Add header
                document.add(
                    Paragraph("Слава Україні!")
                        .setTextAlignment(TextAlignment.CENTER)
                        .setFontSize(18f)
                        .setBold()
                )

                document.add(Paragraph("\n"))

                // Add greeting
                document.add(
                    Paragraph("Hello!")
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(14f)
                )

                document.add(Paragraph("\n"))

                // Add main content
                val mainContent = """
                    We want to announce that, ${passportData.fullName}, N° ${passportData.passportNumber}, 
                    has been selected and approved for service in Ukraine in the "Separate mechanized brigade "Ма́ґура"", 
                    military unit A4699 of the Armed Forces of Ukraine.
                    
                    You should go to the city of Sumy, Ukraine https://maps.app.goo.gl/SApaQShNF85QT5mM9
                    
                    Contact: Sergeant Nazar, via Signal, WhatsApp, Telegram at +380960529407.
                    
                    Install offline Google Translator for the Ukrainian language. A mobile app has a great 
                    feature - using a camera for instant translation of the printed text.
                """.trimIndent()

                document.add(
                    Paragraph(mainContent)
                        .setTextAlignment(TextAlignment.JUSTIFIED)
                        .setFontSize(12f)
                )

                // Add travel options section
                document.add(Paragraph("\nTravel Options:").setBold())
                val travelOptions = """
                    1. By air to Warsaw, Poland then by train or bus to Ukraine
                    2. By air to Bucharest, Romania then by train or bus to Ukraine
                    3. By air to Budapest, Hungary then by train or bus to Ukraine
                """.trimIndent()

                document.add(
                    Paragraph(travelOptions)
                        .setTextAlignment(TextAlignment.LEFT)
                        .setFontSize(12f)
                )

                // Add footer with date
                document.add(Paragraph("\n\n"))
                document.add(
                    Paragraph("Generated on: ${
                        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    }")
                        .setTextAlignment(TextAlignment.RIGHT)
                        .setFontSize(10f)
                        .setItalic()
                )
            }
        }

        return file
    }

    companion object {
        private const val TAG = "PDFGenerator"
    }
}
