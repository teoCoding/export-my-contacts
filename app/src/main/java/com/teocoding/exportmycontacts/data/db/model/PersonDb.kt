package com.teocoding.exportmycontacts.data.db.model

import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey

@PersistedName("people")
class PersonDb: RealmObject {

    @PrimaryKey
    @PersistedName("_id")
    var lookupKey: String? = null
    @PersistedName("name")
    var name: String? = null
    @PersistedName("photoPath")
    var photoPath: String? = null
    @PersistedName("phoneNumbers")
    var phoneNumbers: RealmList<PhoneContactDb> = realmListOf()
    @PersistedName("emails")
    var emails: RealmList<EmailContactDb> = realmListOf()

}
