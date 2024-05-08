package com.teocoding.exportmycontacts.data.repository

import com.teocoding.exportmycontacts.data.db.model.EmailContactDb
import com.teocoding.exportmycontacts.data.db.model.PersonDb
import com.teocoding.exportmycontacts.data.db.model.PhoneContactDb
import com.teocoding.exportmycontacts.data.importer.contacts.PhoneBookImporter
import com.teocoding.exportmycontacts.data.mapper.toEmail
import com.teocoding.exportmycontacts.data.mapper.toPerson
import com.teocoding.exportmycontacts.data.mapper.toPhoneNumber
import com.teocoding.exportmycontacts.domain.model.PersonWithContacts
import com.teocoding.exportmycontacts.domain.repository.ContactRepository
import io.realm.kotlin.Realm
import io.realm.kotlin.UpdatePolicy
import io.realm.kotlin.ext.query
import io.realm.kotlin.ext.toRealmList
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class ContactRepositoryImpl @Inject constructor(
    private val contactsDb: Realm,
    private val phoneBookImporter: PhoneBookImporter
): ContactRepository {

    override suspend fun fetchContactsFromPhoneBook() {

        val phoneBookContacts = phoneBookImporter.importContacts()

        contactsDb.write {

            phoneBookContacts.forEach { contact ->

                val dbPersonDb = PersonDb().apply {

                    lookupKey = contact.lookupKey
                    name = contact.name
                    photoPath = contact.photoUri

                    phoneNumbers = contact.phoneNumbers.map { phone ->
                        PhoneContactDb().apply {

                            phoneNumber = phone.phoneNumber
                            phoneLabel = phone.phoneLabel
                            accountType = phone.accountType
                            accountName = phone.accountName

                        }
                    }.toRealmList()

                    emails = contact.emails.map { emailFromPhoneBook ->
                        EmailContactDb().apply {

                            email = emailFromPhoneBook.email
                            emailLabel = emailFromPhoneBook.emailLabel
                            accountType = emailFromPhoneBook.accountType
                            accountName = emailFromPhoneBook.accountName

                        }
                    }.toRealmList()

                }

                copyToRealm(dbPersonDb, UpdatePolicy.ALL)
            }

        }

    }

    override fun getAllContacts(): Flow<List<PersonWithContacts>> {
        return contactsDb.query<PersonDb>().sort("name")
            .asFlow()
            .map { result ->

                result.list.map { person ->
                    PersonWithContacts(
                        person = person.toPerson(),
                        phoneNumbers = person.phoneNumbers.map { it.toPhoneNumber() },
                        emails = person.emails.map { it.toEmail() }
                    )
                }

            }
    }

    override fun getPeopleWhereId(ids: List<String>): Flow<List<PersonWithContacts>> {

        return contactsDb.query<PersonDb>(
            "lookupKey IN $0",
            ids
        ).sort("name")
            .asFlow()
            .map { result ->

                result.list.map { person ->
                    PersonWithContacts(
                        person = person.toPerson(),
                        phoneNumbers = person.phoneNumbers.map { it.toPhoneNumber() },
                        emails = person.emails.map { it.toEmail() }
                    )
                }

            }
    }
}