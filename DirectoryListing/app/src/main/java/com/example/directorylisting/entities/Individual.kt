package com.example.directorylisting.entities

import com.example.directorylisting.application.R
import com.example.directorylisting.helpers.StringHelper
import io.realm.Realm
import io.realm.RealmObject
import io.realm.annotations.PrimaryKey

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

open class Individual : RealmObject() {

    @PrimaryKey
    var id = ""
    var firstName: String = ""
    var lastName: String = ""
    var birthdate = ""
    var profilePicture = ""
    var affiliation = ""
    var imageCheck = ""
    var forceSensitive = false

    val prettyFullName: String?
        get() {
            if (lastName.isEmpty()) {
                return firstName
            } else if (firstName.isEmpty()) {
                return lastName
            } else return firstName + " " + lastName
        }

    val prettyAffiliationText: String
        get() = StringHelper.identifierToText(affiliation)

    val prettyProfilePicture: String
        get() = if (profilePicture.isEmpty()) {
            StringHelper.getResourcePath(R.drawable.missing)
        } else profilePicture

    fun clear(): Individual {
        id = ""
        firstName = ""
        lastName = ""
        birthdate = ""
        profilePicture = ""
        affiliation = ""
        imageCheck = ""
        forceSensitive = false
        return this
    }

    fun safeForRealm(): Individual {
        val realm = Realm.getDefaultInstance()
        return if (this.isManaged) realm.copyFromRealm(this) else this
    }

}
