package com.example.directorylisting.entities;

import android.graphics.Bitmap;
import android.net.Uri;

import com.example.directorylisting.application.R;
import com.example.directorylisting.helpers.StringHelper;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.Gson;

import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class Individual extends RealmObject {

    @PrimaryKey
    public String id = "";
    public String firstName = "";
    public String lastName = "";
    public String birthdate = "";
    public String profilePicture = "";
    public String affiliation = "";
    public String imageCheck = "";
    public boolean forceSensitive = false;

    public String getId() { // Needed for primary key
        return id;
    }

    public void setId(String value) { // Needed for primary key
        id = value;
    }

    public String getPrettyFullName() {
        if (lastName == null || lastName.isEmpty()) {
            return firstName;
        }

        if (firstName == null || firstName.isEmpty()) {
            return lastName;
        }

        return firstName + " " + lastName;
    }

    public String getPrettyBirthDate() {
        return birthdate; // TODO: Clean up
    }

    public String getPrettyAffiliationText() {
        return StringHelper.identifierToText(affiliation);
    }

    public String getPrettyProfilePicture() {
        if (profilePicture.isEmpty()) {
            return StringHelper.getResourcePath(R.drawable.missing);
        }

        return profilePicture;
    }

    public Individual clear() {
        id = "";
        firstName = "";
        lastName = "";
        birthdate = "";
        profilePicture = "";
        affiliation = "";
        imageCheck = "";
        forceSensitive = false;
        return this;
    }

    public Individual safeForRealm() {
        Realm realm = Realm.getDefaultInstance();
        return this.isManaged() ? realm.copyFromRealm(this) : this;
    }

}
