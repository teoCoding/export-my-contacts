package com.teocoding.exportmycontacts.presentation.screen.contacts_selector

import com.teocoding.exportmycontacts.domain.model.PersonWithContacts

sealed interface ContactsSelectorEvent {

    data class OnPersonSelectChange(val person: PersonWithContacts): ContactsSelectorEvent
    data class OnSelectAllChange(val isSelected: Boolean): ContactsSelectorEvent
    data object DismissErrorDialog: ContactsSelectorEvent
    data object OnExportContacts: ContactsSelectorEvent
}