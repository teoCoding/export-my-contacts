package com.teocoding.exportmycontacts.data.mapper

import com.teocoding.exportmycontacts.data.db.model.EmailContactDb
import com.teocoding.exportmycontacts.domain.model.Email

fun EmailContactDb.toEmail(): Email {

    return Email(
        id = this.id.toString(),
        emailLabel = this.emailLabel,
        email = this.email,
        accountType = this.accountType,
        accountName = this.accountName
    )
}