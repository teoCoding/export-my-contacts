package com.teocoding.exportmycontacts.data.importer.contacts.model

data class PhoneBookContact(
    val lookupKey: String,
    val name: String?,
    val photoUri: String?,
    val phoneNumbers: List<PhoneBookPhoneNumber>,
    val emails: List<PhoneBookEmail>,
)
