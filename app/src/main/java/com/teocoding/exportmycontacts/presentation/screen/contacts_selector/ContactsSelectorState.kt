package com.teocoding.exportmycontacts.presentation.screen.contacts_selector

import androidx.compose.runtime.Immutable
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts

@Immutable
data class ContactsSelectorState (
    val contacts: List<PersonWithContacts> = emptyList(),
    val selectedContacts: List<PersonWithContacts> = emptyList(),
    val isLoading: Boolean = false,
    val isSelectedContactsEmptyError: Boolean = false,
    val contactsToExport: List<String>? = null
)