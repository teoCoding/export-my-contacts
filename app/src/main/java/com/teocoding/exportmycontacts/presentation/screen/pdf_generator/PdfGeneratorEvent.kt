package com.teocoding.exportmycontacts.presentation.screen.pdf_generator

import android.graphics.Picture
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts

sealed interface PdfGeneratorEvent {

    data class OnContentMeasured(
        val people: List<List<PersonWithContacts>>
    ) : PdfGeneratorEvent

    data class OnFirstPageCreated(
        val picture: Picture
    ) : PdfGeneratorEvent

    data class OnPictureTaken(
        val picture: Picture
    ) : PdfGeneratorEvent
}