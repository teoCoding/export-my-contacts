package com.teocoding.exportmycontacts.data.file

import android.content.Context
import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import android.net.Uri
import com.teocoding.exportmycontacts.domain.PdfCacheService
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject

@ViewModelScoped
class PdfCacheServiceImpl @Inject constructor(
    @ApplicationContext private val context: Context
): PdfCacheService {

    override suspend fun cachePdf(pdfDocument: PdfDocument): File {

        return withContext(Dispatchers.IO) {
            val pdfDirectory = File(context.cacheDir, PDF_CACHE_DIR)

            if (!pdfDirectory.exists()) {
                pdfDirectory.mkdir()
            }

            val pdfFile = File(
                pdfDirectory,
                "pdf_${System.currentTimeMillis()}.pdf"
            )

            pdfDocument.writeTo(pdfFile.outputStream())

            pdfFile
        }

    }

    override suspend fun cachePdfBitmaps(bitmaps: List<Bitmap>): List<File> {

        return withContext(Dispatchers.IO) {

            val bitmapDirectory = File(context.cacheDir, BITMAP_CACHE_DIR)

            if (!bitmapDirectory.exists()) {

                bitmapDirectory.mkdir()

            }

            bitmaps.mapIndexed { index, bitmap ->
                val file = File(
                    bitmapDirectory,
                    "contacts_pdf_bitmap-${System.currentTimeMillis()}-$index.jpg"
                )

                val outputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                bitmap.recycle()
                outputStream.flush()
                outputStream.close()

                file
            }
        }
    }

    override suspend fun savePdf(
        sourceFile: File,
        destinationUri: String
    ) {

        val uri = Uri.parse(destinationUri)

        withContext(Dispatchers.IO) {
            val contentResolver = context.contentResolver

            val inputStream = sourceFile.inputStream()
            val outputStream = contentResolver.openOutputStream(uri)

            inputStream.copyTo(outputStream!!)

            inputStream.close()
            outputStream.close()
        }

    }


    private companion object {

        const val PDF_CACHE_DIR = "contacts_pdf"
        const val BITMAP_CACHE_DIR = "cache_bitmaps"

    }
}