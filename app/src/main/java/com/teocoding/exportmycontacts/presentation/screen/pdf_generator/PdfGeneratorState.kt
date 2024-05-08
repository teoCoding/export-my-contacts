package com.teocoding.exportmycontacts.presentation.screen.pdf_generator

import androidx.compose.runtime.Immutable
import com.teocoding.exportmycontacts.domain.PdfPageSize
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts

@Immutable
data class PdfGeneratorState(
    val people: List<PersonWithContacts> = emptyList(),
    val measuredContent: List<List<PersonWithContacts>> = emptyList(),
    val currentProcessedIndex: Int = -1,
    val progress: Float = 0.0f,
    val pdfPageSize: PdfPageSize = PdfPageSize.FormatA4,
    val filePath: String? = null
)
