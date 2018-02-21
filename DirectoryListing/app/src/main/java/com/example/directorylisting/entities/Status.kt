/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.entities

import io.realm.RealmObject

open class Status : RealmObject() {
    var server = ""
    var message = ""
}
