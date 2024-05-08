package com.teocoding.exportmycontacts.domain

import android.graphics.Bitmap
import android.graphics.pdf.PdfDocument
import java.io.File

interface PdfCacheService {

    suspend fun cachePdf(pdfDocument: PdfDocument): File

    suspend fun cachePdfBitmaps(bitmaps: List<Bitmap>): List<File>

    suspend fun savePdf(
        sourceFile: File,
        destinationUri: String
    )
}