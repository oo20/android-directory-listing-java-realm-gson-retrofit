package com.example.directorylisting.shared;

import android.app.ProgressDialog;
import android.content.Context;

import com.bumptech.glide.load.Key;
import com.bumptech.glide.signature.StringSignature;
import com.example.directorylisting.api.WebService;
import com.example.directorylisting.entities.Individual;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class AppManager extends Object {

    static public AppManager shared = new AppManager();

    public WebService webService = null;

    public String hostname = "localhost";
    public boolean allowInvalidCert = true;
    public String appUser = "test"; // TODO: Add login interface when auth is required (only if server prompts).
    public String appPassword = "test"; // TODO: Add login interface when auth is required (only if server prompts).

    public String baseURL = "https://" + hostname + ":8443/api/";
    public String invalidCertURL = "http://" + hostname + ":8080/static/server.crt";

    public Boolean directoryListingRefreshNeeded = false;

    public int imageCacheSize = 100 * 1024 * 1024;
    public int imageCompressionPercentage = 1;

    public ProgressDialog progressDialog = null;

    public static String packageName = null;

    public AppManager init(Context context, WebService.BasicSuccessFailureInterface basicSuccessFailureInterface) {
        webService = new WebService();
        webService.initService(context, 0, basicSuccessFailureInterface);

        return this;
    }

    /*
    Glide 4
    public Key getCacheKey(Individual individual) {
        return new ObjectKey(individual.imageCheck);
    }*/


    //Glide 3
    public Key getCacheKey(Individual individual) {
        return new StringSignature(individual.imageCheck);
    }

    public void showprogressDialog(Context context, String title, String message) {
        Boolean showDialog = false;

        if (progressDialog == null) {
            progressDialog = new ProgressDialog(context);
            showDialog = true;
        }

        progressDialog.setTitle(title);
        progressDialog.setMessage(message);
        progressDialog.setCancelable(false);

        if (showDialog == true) {

            progressDialog.show();
        }
    }

    public void dismissProgressDialog() {
        if (progressDialog == null) {
            return;
        }
        progressDialog.dismiss();
        progressDialog = null;
    }

}
