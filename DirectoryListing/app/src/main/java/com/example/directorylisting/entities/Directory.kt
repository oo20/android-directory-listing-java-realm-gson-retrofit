package com.example.directorylisting.entities

import io.realm.RealmList
import io.realm.RealmObject

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

open class Directory : RealmObject() {

    var individuals: RealmList<Individual> = RealmList<Individual>()

    fun clear(): Directory {
        individuals = RealmList()
        return this
    }

}
