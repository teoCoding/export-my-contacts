package com.teocoding.exportmycontacts.domain.use_case

import com.teocoding.exportmycontacts.domain.model.PersonWithContacts
import javax.inject.Inject

class ContactSelectionValidation @Inject constructor() {

    fun execute(contacts: List<PersonWithContacts>): Result {

        if (contacts.isEmpty()) {
            return Result.EmptyListError
        }

        return Result.Success
    }


    sealed interface Result {

        data object Success : Result
        data object EmptyListError : Result

    }
}


