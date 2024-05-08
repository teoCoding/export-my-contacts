package com.teocoding.exportmycontacts.domain.model

data class PersonWithContacts(
    val person: Person,
    val phoneNumbers: List<PhoneNumber>,
    val emails: List<Email>
)
