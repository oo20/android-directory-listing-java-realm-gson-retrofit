package com.example.directorylisting.shared;

import android.app.ProgressDialog;
import android.content.Context;
import android.widget.ProgressBar;

import com.bumptech.glide.load.Key;
import com.example.directorylisting.api.WebService;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.text.SimpleDateFormat;
import java.util.Date;

import okhttp3.OkHttpClient;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import com.bumptech.glide.signature.ObjectKey;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class AppManager extends Object {

    static public AppManager shared = new AppManager();

    public WebService webService = null;
    public String baseURL = "http://localhost:8080/api/";

    public Boolean directoryListingRefreshNeeded = false;

    public int imageCompressionPercentage = 1;

    public ProgressDialog progressDialog = null;

    public static String packageName = null;

    public AppManager init() {
        webService = new WebService();
        webService.init();

        return this;
    }

    public Key getCacheKey(Individual individual) {
        return new ObjectKey(individual.imageCheck);
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
        progressDialog.dismiss();
        progressDialog = null;
    }

}
