package com.teocoding.exportmycontacts.data.db.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

@PersistedName("emails")
class EmailContactDb: RealmObject {

    @PrimaryKey
    @PersistedName("_id")
    var id: ObjectId = ObjectId()
    @PersistedName("email")
    var email: String? = null
    @PersistedName("emailLabel")
    var emailLabel: String? = null
    @PersistedName("accountType")
    var accountType: String? = null
    @PersistedName("accountName")
    var accountName: String? = null

}
