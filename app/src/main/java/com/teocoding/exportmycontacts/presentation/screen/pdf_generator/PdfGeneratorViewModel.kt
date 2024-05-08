package com.teocoding.exportmycontacts.presentation.screen.pdf_generator

import android.graphics.Picture
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teocoding.exportmycontacts.Screen
import com.teocoding.exportmycontacts.domain.PdfCacheService
import com.teocoding.exportmycontacts.domain.repository.ContactRepository
import com.teocoding.exportmycontacts.presentation.PdfService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class PdfGeneratorViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    contactsRepository: ContactRepository,
    private val pdfService: PdfService,
    private val pdfCacheService: PdfCacheService
) : ViewModel() {

    private val _screenState = MutableStateFlow(PdfGeneratorState())
    val screenState: StateFlow<PdfGeneratorState> = _screenState.asStateFlow()

    private var firstPagePicture: Picture? = null
    private val picturesMap = mutableMapOf<Int, Picture?>()


    init {
        savedStateHandle.get<String>(Screen.PdfGenerator.CONTACTS_IDS)?.let {

            val contactIdsList = it.split(",").map { it.trim() }

            contactsRepository.getPeopleWhereId(contactIdsList)
                .onEach { people ->
                    _screenState.update { state ->
                        state.copy(
                            people = people
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun onEvent(event: PdfGeneratorEvent) {

        when (event) {

            is PdfGeneratorEvent.OnContentMeasured -> {

                if (_screenState.value.measuredContent.isEmpty()) {

                    (0..event.people.lastIndex).forEach {
                        picturesMap[it] = null
                    }

                    _screenState.update { state ->
                        state.copy(
                            measuredContent = event.people,
                            currentProcessedIndex = 0
                        )
                    }
                }

            }

            is PdfGeneratorEvent.OnFirstPageCreated -> {
                firstPagePicture = event.picture
            }


            is PdfGeneratorEvent.OnPictureTaken -> {

                val currentIndex = _screenState.value.currentProcessedIndex

                if (picturesMap[currentIndex] == null) {

                    picturesMap[currentIndex] = event.picture

                    if (currentIndex < _screenState.value.measuredContent.lastIndex) {

                        val progress = currentIndex / _screenState.value.measuredContent.size.toFloat()

                        _screenState.update { state ->
                            state.copy(
                                currentProcessedIndex = currentIndex + 1,
                                progress = progress
                            )

                        }
                    }

                    if (picturesMap.values.all { it != null }) {

                        viewModelScope.launch {

                            val pdfDocument = pdfService.createPdf(
                                pdfPageSize = _screenState.value.pdfPageSize,
                                pictures = listOf(firstPagePicture!!) + picturesMap.map { it.value!! }
                            )

                            val pdfFile = pdfCacheService.cachePdf(pdfDocument)

                            pdfDocument.close()

                            _screenState.update { state ->
                                state.copy(
                                    filePath = pdfFile.path
                                )
                            }
                        }

                    }

                }

            }

        }
    }

}