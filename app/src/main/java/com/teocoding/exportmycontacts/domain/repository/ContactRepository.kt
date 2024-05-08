package com.teocoding.exportmycontacts.domain.repository

import com.teocoding.exportmycontacts.domain.model.PersonWithContacts
import kotlinx.coroutines.flow.Flow

interface ContactRepository {

    suspend fun fetchContactsFromPhoneBook()

    fun getAllContacts(): Flow<List<PersonWithContacts>>

    fun getPeopleWhereId(ids: List<String>): Flow<List<PersonWithContacts>>

}