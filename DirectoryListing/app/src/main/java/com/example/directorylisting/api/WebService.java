package com.example.directorylisting.api;

import android.util.Log;

import com.example.directorylisting.application.BuildConfig;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.ArrayList;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmModel;
import io.realm.RealmObject;
import io.realm.RealmResults;
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

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class WebService {

    public WebServiceInterface webServiceInterface = null;

    public interface IndividualsInterface {
        void onResponse(List<Individual> individuals);
        void onFailure();
    }

    public interface IndividualInterface {
        void onResponse(Individual individual);
        void onFailure();
    }

    public interface BasicSuccessFailureInterface {
        void onSuccess();
        void onFailure();
    }

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

    private Call<Directory> getIndividuals() {
        return webServiceInterface.getIndividuals();
    }

    public void getIndividuals(final IndividualsInterface individualsInterface) {
        clear(Individual.class);
        fetchIndividuals(individualsInterface);
    }

    public void fetchIndividuals(final IndividualsInterface individualsInterface) {

        Realm realm = Realm.getDefaultInstance();

        RealmResults<Individual> storedIndividuals = realm.where(Individual.class).findAll();

        if (storedIndividuals.size() > 0) {
            individualsInterface.onResponse(WebService.convertResultsToList(storedIndividuals));
            return;
        }

        webServiceInterface.getIndividuals().enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Call<Directory> call, Response<Directory> response) {
                Log.d(WebService.class.toString(), "Response:" + response.toString());

                Directory directory = response.body();

                final List<Individual> individuals = new ArrayList<Individual>();

                Realm realm = Realm.getDefaultInstance();

                realm.beginTransaction();
                for (Individual individual : directory.individuals) {
                    individuals.add(individual);
                    realm.insertOrUpdate(individual.safeForRealm());
                }
                realm.commitTransaction();

                individualsInterface.onResponse(individuals);
            }

            @Override
            public void onFailure(Call<Directory> call, Throwable t) {
                Log.d(WebService.class.toString(), "Failure");

                individualsInterface.onFailure();
            }
        });
    }

    private Call<Directory> createIndividual(Individual individual) {
        Realm realm = Realm.getDefaultInstance();

        Directory directory = new Directory().clear();
        directory.individuals.add(individual);
        return webServiceInterface.createIndividual("application/json", new Gson().toJson(directory));
    }

    public void createIndividual(Individual individual, IndividualInterface individualInterface) {
        assert(individual.getId().isEmpty() == true); // Want to maintain calling function distinction in the app
        saveIndividual(individual, individualInterface);
    }

    private void saveIndividual(final Individual individual, final IndividualInterface individualInterface) {

        Callback<Directory> responseCallback = new Callback<Directory>() {
            @Override
            public void onResponse(Call<Directory> call, Response<Directory> response) {
                Log.d(WebService.class.toString(), "Response:" + response.toString());

                Directory directory = response.body();

                final List<Individual> individuals = new ArrayList<Individual>();

                if (directory.individuals.size() <= 0) {
                    Log.d(WebService.class.toString(), "Failed to save individual properly.  Individual not returned from server.");

                    individualInterface.onFailure();
                    return;
                }

                Realm realm = Realm.getDefaultInstance();

                realm.beginTransaction();
                for (Individual listedIndividual : directory.individuals) {
                    individuals.add(listedIndividual);
                    realm.insertOrUpdate(listedIndividual.safeForRealm());
                }
                realm.commitTransaction();

                individualInterface.onResponse((Individual) individuals.get(0).safeForRealm());
            }

            @Override
            public void onFailure(Call<Directory> call, Throwable t) {
                Log.d(WebService.class.toString(), "Failed to save individual.");

                individualInterface.onFailure();
            }
        };

        if (individual.getId().isEmpty()) {
            Log.d(WebService.class.toString(), "Creating Individual");

            AppManager.shared.webService.createIndividual(individual).enqueue(responseCallback);
        } else {
            Log.d(WebService.class.toString(), "Modifying Individual: " + individual.id);

            AppManager.shared.webService.modifyIndividual(individual.id, individual).enqueue(responseCallback);
        }

    }

    private Call<Directory> modifyIndividual(String id, Individual individual) {
        Directory directory = new Directory().clear();
        directory.individuals.add(individual);
        return webServiceInterface.modifyIndividual("application/json", id, new Gson().toJson(directory));
    }

    public void modifyIndividual(String id, Individual individual, IndividualInterface individualInterface) {
        assert(individual.getId().isEmpty() == false); // Want to maintain calling function distinction in the app
        saveIndividual(individual, individualInterface);
    }

    private void deleteIndividual(String id, final BasicSuccessFailureInterface deleteIndividualInterface) {
        webServiceInterface.deleteIndividual(id).enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Call<Directory> call, Response<Directory> response) {
                deleteIndividualInterface.onSuccess();
            }

            @Override
            public void onFailure(Call<Directory> call, Throwable t) {
                deleteIndividualInterface.onFailure();
            }
        });
    }

    public void uploadFile(String id, String fileData, final BasicSuccessFailureInterface uploadFileInterface) {
        webServiceInterface.uploadFile(id, MultipartBody.Part.createFormData("tempFile", fileData)).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(WebService.class.toString(), "Saving photo response:" + response.toString());

                uploadFileInterface.onSuccess();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d(WebService.class.toString(), "Failed to save photo.");

                uploadFileInterface.onFailure();
            }
        });
    }


    public void clear(Class<? extends RealmModel> clazz) {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.delete(clazz);
        realm.commitTransaction();
    }

    public void clearData() {
        Realm realm = Realm.getDefaultInstance();

        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    public static <T extends Individual> List<T> convertResultsToList(RealmResults<T> results) {
        Realm realm = Realm.getDefaultInstance();

        List<T> list = new ArrayList<T>();
        for (T result : results) {
            list.add((T)result.safeForRealm());
        }
        return list;
    }

}