package com.teocoding.exportmycontacts.presentation.screen.pdf_preview

import androidx.compose.runtime.Immutable

@Immutable
data class PdfPreviewState(
    val bitmapFiles: List<String> = emptyList()
)
