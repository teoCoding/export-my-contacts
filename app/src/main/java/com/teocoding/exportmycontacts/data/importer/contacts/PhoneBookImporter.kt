package com.teocoding.exportmycontacts.data.importer.contacts

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.telephony.PhoneNumberUtils
import com.teocoding.exportmycontacts.data.importer.contacts.model.PhoneBookContact
import com.teocoding.exportmycontacts.data.importer.contacts.model.PhoneBookEmail
import com.teocoding.exportmycontacts.data.importer.contacts.model.PhoneBookPhoneNumber
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject


class PhoneBookImporter @Inject constructor(
    @ApplicationContext private val context: Context
) {


    suspend fun importContacts(): List<PhoneBookContact> {

        return withContext(Dispatchers.IO) {

            val contacts = fetchContacts()

            contacts.map { phoneBookContact ->

                val phoneNumbers = fetchPhoneNumbersForContact(phoneBookContact.lookupKey)

                val emails = fetchEmailsForContact(phoneBookContact.lookupKey)

                phoneBookContact.copy(
                    phoneNumbers = phoneNumbers,
                    emails = emails
                )
            }

        }
    }


    private fun fetchContacts(): List<PhoneBookContact> {

        val contactsList = mutableListOf<PhoneBookContact>()
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(
                ContactsContract.Contacts.LOOKUP_KEY,
                ContactsContract.Contacts.DISPLAY_NAME,
                ContactsContract.Contacts.PHOTO_URI,
            ),
            null,
            null,
            null
        )

        cursor?.use { innerCursor ->

            if (innerCursor.moveToFirst()) {

                do {

                    val lookupKey = innerCursor
                        .getString(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.Contacts.LOOKUP_KEY
                            )
                        )

                    val displayName = innerCursor
                        .getString(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.Contacts.DISPLAY_NAME
                            )
                        )

                    val photoUri = innerCursor
                        .getString(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.Contacts.PHOTO_URI
                            )
                        )

                    val phoneBookContact = PhoneBookContact(
                        lookupKey = lookupKey,
                        name = displayName,
                        photoUri = photoUri,
                        phoneNumbers = emptyList(),
                        emails = emptyList()
                    )

                    contactsList.add(phoneBookContact)

                } while (innerCursor.moveToNext())
            }

        }

        cursor?.close()

        return contactsList.toList()

    }

    private fun fetchPhoneNumbersForContact(lookupKey: String): List<PhoneBookPhoneNumber> {

        val phoneNumberList = mutableListOf<PhoneBookPhoneNumber>()
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.LABEL,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.RawContacts.ACCOUNT_NAME
            ),
            ContactsContract.CommonDataKinds.Phone.LOOKUP_KEY + " = ?",
            arrayOf(lookupKey),
            null
        )


        cursor?.use { innerCursor ->

            if (innerCursor.moveToFirst()) {

                do {

                    val phoneNumber = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                    )

                    val type =
                        innerCursor.getInt(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.TYPE
                            )
                        )

                    val label =
                        innerCursor.getString(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Phone.LABEL
                            )
                        )


                    val accountType = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)
                    )

                    val accountName = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)
                    )

                    val normalizedPhoneNumber = PhoneNumberUtils.normalizeNumber(phoneNumber)


                    val phoneLabel = ContactsContract.CommonDataKinds.Phone.getTypeLabel(context.resources, type, label).toString()

                    val phoneBookPhoneNumber = PhoneBookPhoneNumber(
                        phoneNumber = normalizedPhoneNumber,
                        phoneLabel = phoneLabel,
                        accountName = accountName,
                        accountType = accountType
                    )

                    phoneNumberList.add(phoneBookPhoneNumber)

                } while (innerCursor.moveToNext())
            }

        }

        cursor?.close()

        return phoneNumberList

    }


    private fun fetchEmailsForContact(lookupKey: String): List<PhoneBookEmail> {

        val emailList = mutableListOf<PhoneBookEmail>()
        val contentResolver: ContentResolver = context.contentResolver
        val cursor: Cursor? = contentResolver.query(
            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.TYPE,
                ContactsContract.CommonDataKinds.Email.LABEL,
                ContactsContract.RawContacts.ACCOUNT_TYPE,
                ContactsContract.RawContacts.ACCOUNT_NAME
            ),
            ContactsContract.CommonDataKinds.Email.LOOKUP_KEY + " = ?",
            arrayOf(lookupKey),
            null
        )

        cursor?.use { innerCursor ->

            if (innerCursor.moveToFirst()) {

                do {

                    val email = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Email.ADDRESS)
                    )

                    val type =
                        innerCursor.getInt(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Email.TYPE
                            )
                        )

                    val label =
                        innerCursor.getString(
                            innerCursor.getColumnIndexOrThrow(
                                ContactsContract.CommonDataKinds.Email.LABEL
                            )
                        )


                    val accountType = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_TYPE)
                    )

                    val accountName = innerCursor.getString(
                        innerCursor.getColumnIndexOrThrow(ContactsContract.RawContacts.ACCOUNT_NAME)
                    )

                    val emailLabel = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.resources, type, label).toString()

                    val phoneBookEmail = PhoneBookEmail(
                        email = email,
                        emailLabel = emailLabel,
                        accountName = accountName,
                        accountType = accountType
                    )

                    emailList.add(phoneBookEmail)

                } while (innerCursor.moveToNext())
            }

        }

        cursor?.close()

        return emailList

    }
}

