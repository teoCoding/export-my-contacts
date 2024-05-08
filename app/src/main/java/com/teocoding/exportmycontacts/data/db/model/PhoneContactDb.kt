package com.teocoding.exportmycontacts.data.db.model

import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PersistedName
import io.realm.kotlin.types.annotations.PrimaryKey
import org.mongodb.kbson.ObjectId

@PersistedName("phones")
class PhoneContactDb: RealmObject {

    @PrimaryKey
    @PersistedName("_id")
    var id: ObjectId = ObjectId()
    @PersistedName("phoneNumber")
    var phoneNumber: String? = null
    @PersistedName("phoneLabel")
    var phoneLabel: String? = null
    @PersistedName("accountType")
    var accountType: String? = null
    @PersistedName("accountName")
    var accountName: String? = null

}
