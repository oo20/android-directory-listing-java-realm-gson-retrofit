package com.example.directorylisting.entities;

import io.realm.RealmList;
import io.realm.RealmObject;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class Directory extends RealmObject {

    public RealmList<Individual> individuals;

    public Directory clear() {
        individuals = new RealmList<Individual>();
        return this;
    }

}
