package com.teocoding.exportmycontacts.presentation.screen.pdf_preview

import android.net.Uri

sealed interface PdfPreviewEvent {

    data class OnSavePdf(
        val fileUri: Uri
    ) : PdfPreviewEvent
}