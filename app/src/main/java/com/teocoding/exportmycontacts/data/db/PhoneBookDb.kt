package com.teocoding.exportmycontacts.data.db

import com.teocoding.exportmycontacts.data.db.model.EmailContactDb
import com.teocoding.exportmycontacts.data.db.model.PersonDb
import com.teocoding.exportmycontacts.data.db.model.PhoneContactDb
import io.realm.kotlin.Realm
import io.realm.kotlin.RealmConfiguration

private const val PHONEBOOK_DB_VERSION = 0L


object PhoneBookDb {

    private val config = RealmConfiguration.Builder(
        schema = setOf(
            PersonDb::class, PhoneContactDb::class,
            EmailContactDb::class
        )
    )
        .schemaVersion(PHONEBOOK_DB_VERSION)
        .build()


    val realm = Realm.open(config)

}