package com.teocoding.exportmycontacts.data.mapper

import com.teocoding.exportmycontacts.data.db.model.PhoneContactDb
import com.teocoding.exportmycontacts.domain.model.PhoneNumber

fun PhoneContactDb.toPhoneNumber(): PhoneNumber {

    return PhoneNumber(
        id = this.id.toString(),
        phoneLabel = this.phoneLabel,
        phoneNumber = this.phoneNumber,
        accountType = this.accountType,
        accountName = this.accountName
    )
}