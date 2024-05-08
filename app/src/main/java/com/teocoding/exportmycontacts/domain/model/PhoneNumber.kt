package com.teocoding.exportmycontacts.domain.model

data class PhoneNumber(
    val id: String,
    val phoneNumber: String?,
    val phoneLabel: String?,
    val accountType: String?,
    val accountName: String?
)
