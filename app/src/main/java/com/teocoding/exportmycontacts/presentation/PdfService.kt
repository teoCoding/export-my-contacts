package com.teocoding.exportmycontacts.presentation

import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Picture
import android.graphics.pdf.PdfDocument
import android.graphics.pdf.PdfDocument.PageInfo
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import com.teocoding.exportmycontacts.domain.PdfPageSize
import dagger.hilt.android.scopes.ViewModelScoped
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject


@ViewModelScoped
class PdfService @Inject constructor() {

    suspend fun createPdf(
        pdfPageSize: PdfPageSize,
        pictures: List<Picture>
    ): PdfDocument {

        return withContext(Dispatchers.IO) {
            val pdfDocument = PdfDocument()

            val pageInfo = PageInfo
                .Builder(pdfPageSize.width, pdfPageSize.height, pictures.size)
                .create()

            pictures.forEach { picture ->

                val page = pdfDocument.startPage(pageInfo)
                page.canvas.drawColor(Color.WHITE)
                page.canvas.drawPicture(picture)

                pdfDocument.finishPage(page)
            }

            pdfDocument

        }

    }


    fun renderPdf(
        filePath: String
    ): List<Bitmap> {

        val file = File(filePath)

        val parcelFileDescriptor =
            ParcelFileDescriptor.open(file, ParcelFileDescriptor.MODE_READ_ONLY)

        val render = PdfRenderer(parcelFileDescriptor)

        val pageCount = render.pageCount

        val bitmaps = (0 until pageCount).map {

            val page = render.openPage(it)
            val bitmap = Bitmap.createBitmap(
                page.width,
                page.height,
                Bitmap.Config.ARGB_8888
            )

            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)

            page.close()

            bitmap

        }

        render.close()

        return bitmaps
    }

}