package com.teocoding.exportmycontacts.data.mapper

import com.teocoding.exportmycontacts.data.db.model.PersonDb
import com.teocoding.exportmycontacts.domain.model.Person

fun PersonDb.toPerson(): Person {

    return Person(
        id = this.lookupKey!!,
        name = this.name,
        photoUri = this.photoPath
    )
}