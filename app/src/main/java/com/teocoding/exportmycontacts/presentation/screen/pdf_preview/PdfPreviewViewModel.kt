package com.teocoding.exportmycontacts.presentation.screen.pdf_preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teocoding.exportmycontacts.Screen
import com.teocoding.exportmycontacts.domain.PdfCacheService
import com.teocoding.exportmycontacts.presentation.PdfService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

@HiltViewModel
class PdfPreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    pdfService: PdfService,
    private val pdfCacheService: PdfCacheService
) : ViewModel() {

    private val _screenState = MutableStateFlow(PdfPreviewState())
    val screenState: StateFlow<PdfPreviewState> = _screenState.asStateFlow()

    private val filePath = checkNotNull(savedStateHandle.get<String>(Screen.PdfPreview.PDF_FILE_PATH))


    init {
        viewModelScope.launch {

            savedStateHandle.get<String>(Screen.PdfPreview.PDF_FILE_PATH)?.let { filePath ->

                val bitmaps = pdfService.renderPdf(filePath)

                val files = pdfCacheService.cachePdfBitmaps(bitmaps)


                _screenState.update { state ->
                    state.copy(
                        bitmapFiles = files.map { it.path }
                    )
                }
            }
        }
    }


    fun onEvent(event: PdfPreviewEvent) {
        when (event) {
            is PdfPreviewEvent.OnSavePdf -> {
                viewModelScope.launch {
                    val pdfFile = File(filePath)

                    pdfCacheService.savePdf(
                        sourceFile = pdfFile,
                        destinationUri = event.fileUri.toString()
                    )

                }
            }

        }
    }


}