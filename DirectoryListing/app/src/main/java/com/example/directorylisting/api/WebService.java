package com.example.directorylisting.api;

import android.icu.text.DateFormat;
import android.util.Log;

import com.example.directorylisting.application.BuildConfig;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import io.realm.Realm;
import io.realm.RealmList;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import retrofit2.http.Part;

/**
 * Created by Michael Steele on 3/22/17.
 */

public class WebService {

    public WebServiceInterface webServiceInterface = null;

    public void init() {

        Gson gson = new GsonBuilder()
                .create();

        OkHttpClient.Builder httpClientBuilder = new OkHttpClient.Builder();

        if (BuildConfig.DEBUG) {
            // enable logging for debug builds
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClientBuilder.addInterceptor(loggingInterceptor);
        }

        webServiceInterface = new Retrofit.Builder()
                .baseUrl(AppManager.shared.baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(httpClientBuilder.build())
                .build().create(WebServiceInterface.class);

    }

    public Call<Directory> getIndividuals() {
        return webServiceInterface.getIndividuals();
    }

    public Call<Directory> createIndividual(Individual individual) {
        Directory directory = new Directory().clear();
        directory.individuals.add(individual);
        return webServiceInterface.createIndividual("application/json", new Gson().toJson(directory));
    }

    public Call<Directory> modifyIndividual(String id, Individual individual) {
        Directory directory = new Directory().clear();
        directory.individuals.add(individual);
        return webServiceInterface.modifyIndividual("application/json", id, new Gson().toJson(directory));
    }

    public Call<Directory> deleteIndividual(String id) {
        return webServiceInterface.deleteIndividual(id);
    }

    public Call<ResponseBody> uploadFile(String id, String fileData) {
        return webServiceInterface.uploadFile(id, MultipartBody.Part.createFormData("tempFile", fileData));
    }
}
