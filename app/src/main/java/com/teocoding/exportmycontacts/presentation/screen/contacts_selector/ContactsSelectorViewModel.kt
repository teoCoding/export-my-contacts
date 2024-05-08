package com.teocoding.exportmycontacts.presentation.screen.contacts_selector

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.teocoding.exportmycontacts.domain.repository.ContactRepository
import com.teocoding.exportmycontacts.domain.use_case.ContactSelectionValidation
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ContactsSelectorViewModel @Inject constructor(
    private val contactRepository: ContactRepository,
    private val contactSelectionValidation: ContactSelectionValidation
) : ViewModel() {

    private val _screenState = MutableStateFlow(ContactsSelectorState())
    val screenState: StateFlow<ContactsSelectorState> = _screenState.asStateFlow()

    init {

        viewModelScope.launch {

            contactRepository.fetchContactsFromPhoneBook()

            contactRepository.getAllContacts()
                .onStart {
                    _screenState.update { state ->
                        state.copy(
                            isLoading = true
                        )
                    }
                }
                .onEach {
                    _screenState.update { state ->
                        state.copy(
                            contacts = it,
                            isLoading = false
                        )
                    }
                }
                .launchIn(viewModelScope)
        }
    }


    fun onEvent(event: ContactsSelectorEvent) {

        when (event) {

            is ContactsSelectorEvent.OnPersonSelectChange -> {

                val newList = if (event.person in _screenState.value.selectedContacts) {
                    _screenState.value.selectedContacts - event.person
                } else {
                    _screenState.value.selectedContacts + event.person
                }

                _screenState.update { state ->
                    state.copy(
                        selectedContacts = newList
                    )
                }
            }

            is ContactsSelectorEvent.OnSelectAllChange -> {

                val newList = if (event.isSelected){
                    _screenState.value.contacts
                } else {
                    emptyList()
                }

                _screenState.update { state ->
                    state.copy(
                        selectedContacts = newList
                    )
                }
            }

            ContactsSelectorEvent.OnExportContacts -> {
                val result = contactSelectionValidation.execute(
                    _screenState.value.selectedContacts
                )

                if (result is ContactSelectionValidation.Result.EmptyListError) {

                    _screenState.update { state ->
                        state.copy(
                            isSelectedContactsEmptyError = true
                        )
                    }

                    return
                }

                _screenState.update { state ->
                    state.copy(
                        contactsToExport = _screenState.value.selectedContacts.map { it.person.id }
                    )
                }
            }

            ContactsSelectorEvent.DismissErrorDialog -> {
                _screenState.update { state ->
                    state.copy(
                        isSelectedContactsEmptyError = false
                    )
                }
            }
        }

    }


    fun onExportContactsProcessed() {
        _screenState.update { state ->
            state.copy(
                contactsToExport = null
            )
        }
    }
}