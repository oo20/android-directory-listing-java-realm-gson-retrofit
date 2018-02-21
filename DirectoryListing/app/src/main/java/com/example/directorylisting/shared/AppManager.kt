package com.example.directorylisting.shared

import android.app.ProgressDialog
import android.content.Context

import com.bumptech.glide.load.Key
import com.bumptech.glide.signature.StringSignature
import com.example.directorylisting.api.WebService
import com.example.directorylisting.entities.Individual

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class AppManager : Any() {

    var webService: WebService = WebService()

    var hostname = "localhost"
    var allowInvalidCert = true
    var appUser = "test" // TODO: Add login interface when auth is required (only if server prompts).
    var appPassword = "test" // TODO: Add login interface when auth is required (only if server prompts).

    var baseURL = "https://$hostname:8443/api/"
    var invalidCertURL = "http://$hostname:8080/static/server.crt"

    var directoryListingRefreshNeeded: Boolean = false

    var imageCacheSize = 100 * 1024 * 1024
    var imageCompressionPercentage = 1

    var progressDialog: ProgressDialog? = null

    fun init(context: Context, basicSuccessFailureInterface: WebService.BasicSuccessFailureInterface): AppManager {
        webService.initService(context, 0, basicSuccessFailureInterface)

        return this
    }

    /*
    Glide 4
    public Key getCacheKey(Individual individual) {
        return new ObjectKey(individual.imageCheck);
    }*/


    //Glide 3
    fun getCacheKey(individual: Individual): Key {
        return StringSignature(individual.imageCheck)
    }

    fun showprogressDialog(context: Context, title: String, message: String) {
        var showDialog: Boolean = false

        showDialog = if (progressDialog == null) true else false

        val dialog = progressDialog?: ProgressDialog(context)

        dialog.setTitle(title)
        dialog.setMessage(message)
        dialog.setCancelable(false)

        if (showDialog == true) {
            dialog.show()
            progressDialog = dialog
        }
    }

    fun dismissProgressDialog() {
        val dialog = progressDialog?: return

        dialog.dismiss()

        progressDialog = null
    }

    companion object {

        var shared = AppManager()

        var packageName: String? = null
    }

}
