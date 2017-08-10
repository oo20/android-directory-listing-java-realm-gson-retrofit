package com.example.directorylisting.api;

import android.content.Context;
import android.util.Log;

import com.burgstaller.okhttp.AuthenticationCacheInterceptor;
import com.burgstaller.okhttp.CachingAuthenticatorDecorator;
import com.burgstaller.okhttp.DispatchingAuthenticator;
import com.burgstaller.okhttp.basic.BasicAuthenticator;
import com.burgstaller.okhttp.digest.CachingAuthenticator;
import com.burgstaller.okhttp.digest.Credentials;
import com.burgstaller.okhttp.digest.DigestAuthenticator;
import com.example.directorylisting.auth.OkHttpUtils;
import com.example.directorylisting.auth.SSLUtils;
import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Individual;
import com.example.directorylisting.entities.Status;
import com.example.directorylisting.helpers.StringHelper;
import com.example.directorylisting.shared.AppManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import io.realm.Realm;
import io.realm.RealmModel;
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

    public WebServiceDetails webDetails = null;
    public WebServiceDetails imageWebDetails = null;
    public WebServiceDetails certDetails = null;

    public interface IndividualsInterface {
        void onResponse(List<Individual> individuals);
        void onFailure();
    }

    public interface IndividualInterface {
        void onResponse(Individual individual);
        void onFailure();
    }

    public interface BasicSuccessObjectFailureInterface {
        void onResponse(InputStream inputStream);
        void onFailure();
    }

    public interface BasicSuccessFailureInterface {
        void onSuccess();
        void onFailure();
    }

    public void checkStatus(WebServiceDetails webServiceDetails, final BasicSuccessFailureInterface basicSuccessFailureInterface) {
        webServiceDetails.service.getStatus().enqueue(new Callback<Status>() {
            @Override
            public void onResponse(Call<Status> call, Response<Status> response) {
                Status status = response.body();
                if (status == null || status.server == null || status.server.equals("ok") == false) {
                    basicSuccessFailureInterface.onFailure();
                    return;
                }
                basicSuccessFailureInterface.onSuccess();
            }

            @Override
            public void onFailure(Call<Status> call, Throwable t) {
                basicSuccessFailureInterface.onFailure();
            }
        });
    }

    private Call<Directory> getIndividuals() {
        return webDetails.service.getIndividuals();
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

        webDetails.service.getIndividuals().enqueue(new Callback<Directory>() {
            @Override
            public void onResponse(Call<Directory> call, Response<Directory> response) {
                Log.d(WebService.class.toString(), "Response:" + response.toString());

                Directory directory = response.body();

                final List<Individual> individuals = new ArrayList<Individual>();

                Realm realm = Realm.getDefaultInstance();

                if (response.code() != 200 || directory.individuals == null) {
                    Log.d(WebService.class.toString(), "Response code: " + String.valueOf(response.code()) + "  Also: Individual list is null. Failure.");

                    individualsInterface.onFailure();
                    return;
                }

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
                Log.d(WebService.class.toString(), "getIndividuals Failure: " + t);

                individualsInterface.onFailure();
            }
        });
    }

    private Call<Directory> createIndividual(Individual individual) {
        Realm realm = Realm.getDefaultInstance();

        Directory directory = new Directory().clear();
        directory.individuals.add(individual);
        return webDetails.service.createIndividual("application/json", new Gson().toJson(directory));
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
        return webDetails.service.modifyIndividual("application/json", id, new Gson().toJson(directory));
    }

    public void modifyIndividual(String id, Individual individual, IndividualInterface individualInterface) {
        assert(individual.getId().isEmpty() == false); // Want to maintain calling function distinction in the app
        saveIndividual(individual, individualInterface);
    }

    public void deleteIndividual(String id, final BasicSuccessFailureInterface deleteIndividualInterface) {
        webDetails.service.deleteIndividual(id).enqueue(new Callback<Directory>() {
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
        webDetails.service.uploadFile(id, MultipartBody.Part.createFormData("tempFile", fileData)).enqueue(new Callback<ResponseBody>() {
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

    public void downloadFile(WebServiceDetails webDetails, final String url, final BasicSuccessObjectFailureInterface downloadFileInterface) {
        webDetails.service.downloadFile(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(WebService.class.toString(), "Downloaded file: " + url);
                downloadFileInterface.onResponse((InputStream)response.body().byteStream());
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(WebService.class.toString(), "Failed to download file: " + url);
                downloadFileInterface.onFailure();
            }
        });
    }

    public void downloadSaveFile(final Context context, WebServiceDetails webDetails, final String url, final BasicSuccessFailureInterface basicSuccessFailureInterface) {
        webDetails.service.downloadFile(url).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                Log.d(WebService.class.toString(), "Downloaded file: " + url);

                String text = null;
                try {
                    text = new String(response.body().bytes());
                } catch (IOException e) {
                    Log.e(WebService.class.toString(), "Failed to convert cert to string: " + url);
                    basicSuccessFailureInterface.onFailure();
                    e.printStackTrace();
                    return;
                }

                writeCert(context, text, new BasicSuccessFailureInterface() {
                    @Override
                    public void onSuccess() {
                        Log.e(WebService.class.toString(), "Wrote downloaded file: " + url);
                        basicSuccessFailureInterface.onSuccess();
                    }

                    @Override
                    public void onFailure() {
                        Log.e(WebService.class.toString(), "Failed to write downloaded file: " + url);
                        basicSuccessFailureInterface.onFailure();
                    }
                });
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(WebService.class.toString(), "Failed to download file: " + url);
                basicSuccessFailureInterface.onFailure();
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

    public WebServiceDetails initWebService(Context context, boolean onlyValidCert, Integer cacheSize) {
        WebServiceDetails details = new WebServiceDetails();

        details.cacheSize = cacheSize;

        Gson gson = new GsonBuilder()
                .create();

        HttpLoggingInterceptor httpLoggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(String message) {
                Log.d(WebService.class.toString(), "TODO: HttpLoggingInterceptor: " + message); // TODO: Implement the ability to start a login activity
            }
        });
        httpLoggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        Credentials credentials = new Credentials(AppManager.shared.appUser, AppManager.shared.appPassword);

        final BasicAuthenticator basicAuthenticator = new BasicAuthenticator(credentials);
        final DigestAuthenticator digestAuthenticator = new DigestAuthenticator(credentials);

        DispatchingAuthenticator authenticator = new DispatchingAuthenticator.Builder()
                .with("digest", digestAuthenticator)
                //.with("basic", basicAuthenticator) // Not needed currently.
                .build();

        final Map<String, CachingAuthenticator> authCache = new ConcurrentHashMap<>();
        final OkHttpClient okHttpClient = new OkHttpClient();
        OkHttpClient okHttpClientBuilder = (onlyValidCert == true ? new OkHttpClient.Builder(): new OkHttpUtils().getUnsafeOkHttpClientBuilder(context, okHttpClient))
                .authenticator(new CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(new AuthenticationCacheInterceptor(authCache))
                .build();

        details.service = new Retrofit.Builder()
                .baseUrl(AppManager.shared.baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(okHttpClientBuilder)
                .build().create(WebServiceInterface.class);
        details.client = okHttpClient;
        details.factory = okHttpClientBuilder;

        return details;
    }

    private void initService(Context context) {
        final boolean onlyValidCert = !AppManager.shared.allowInvalidCert;

        webDetails = initWebService(context, onlyValidCert, 0); // No cache for the API server side.  Using realm database instead.
        imageWebDetails = initWebService(context, onlyValidCert, AppManager.shared.imageCacheSize);
    }

    public void initService(final Context context, final int resourceId, final BasicSuccessFailureInterface basicSuccessFailureInterface) {
        final boolean onlyValidCert = !AppManager.shared.allowInvalidCert;

        if (onlyValidCert == true) {
            initService(context);
            basicSuccessFailureInterface.onSuccess();
            return;
        }

        certDetails = initWebService(context, true, 0);

        InputStream serverCert = getCert(context);

        // Found cached cert
        if (serverCert != null) {
            initService(context);

            checkStatus(webDetails, new BasicSuccessFailureInterface() {
                @Override
                public void onSuccess() {
                    Log.d(WebService.class.toString(), "Using cached resource file.");
                    basicSuccessFailureInterface.onSuccess();
                }

                @Override
                public void onFailure() {
                    Log.d(WebService.class.toString(), "Bad certificate.");
                    getNewCert(context, resourceId, basicSuccessFailureInterface);
                }
            });

            return;
        }

        getNewCert(context, resourceId, basicSuccessFailureInterface);
    }

    private void getNewCert(final Context context, int resourceId, final BasicSuccessFailureInterface basicSuccessFailureInterface) {
        InputStream serverCert = null;

        // Get cert from resource
        if (resourceId > 0) {
            serverCert = SSLUtils.getBufferedInputStreamFromResource(context, resourceId);
            if (serverCert == null) {
                Log.d(WebService.class.toString(), "Failed to get buffered input stream from resource.");
                basicSuccessFailureInterface.onFailure();
                return;
            }
            String text = null;
            try {
                text = StringHelper.streamToText(serverCert);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (text == null) {
                Log.d(WebService.class.toString(), "Failed to get buffered input stream from resource.");
                basicSuccessFailureInterface.onFailure();
                return;
            }

            writeCert(context, text, new BasicSuccessFailureInterface() {
                @Override
                public void onSuccess() {
                    Log.e(WebService.class.toString(), "Wrote resource file.");
                    initService(context);
                    basicSuccessFailureInterface.onSuccess();
                }

                @Override
                public void onFailure() {
                    Log.e(WebService.class.toString(), "Failed to write resource file.");
                    basicSuccessFailureInterface.onFailure();
                }
            });

            return;
        }

        // No resource cert, so downloading cert.
        downloadSaveFile(context, certDetails, AppManager.shared.invalidCertURL, new BasicSuccessFailureInterface() {
            @Override
            public void onSuccess() {
                Log.d(WebService.class.toString(), "Downloaded server cert.");

                initService(context);

                basicSuccessFailureInterface.onSuccess();

            }

            @Override
            public void onFailure() {
                Log.d(WebService.class.toString(), "Failed to download server cert.");

                basicSuccessFailureInterface.onFailure();

            }
        });
    }

    public static void writeCert(Context context, String text, WebService.BasicSuccessFailureInterface basicSuccessFailureInterface) {
        try {
            OutputStream outputStream = new FileOutputStream(certFile(context));
            Writer writer = new OutputStreamWriter(outputStream);

            writer.write(text);

            writer.close();

            basicSuccessFailureInterface.onSuccess();
        } catch (Exception e) {
            Log.e(WebService.class.toString(), "Failed to write downloaded file.");

            e.printStackTrace();

            basicSuccessFailureInterface.onFailure();
        }
    }

    public static InputStream getCertInputStream(Context context) {
        InputStream targetStream = null;
        File initialFile = certFile(context);
        try {
            targetStream = new FileInputStream(initialFile);
            return targetStream;
        } catch (FileNotFoundException e) {
            Log.e(WebService.class.toString(), "Failed to get input stream for file.");
            e.printStackTrace();
        }
        return null;
    }

    public static BufferedInputStream getCert(Context context) {
        BufferedInputStream serverCert = null;
        try {
            InputStream inputStream = getCertInputStream(context);
            if (inputStream == null) {
                Log.d(WebService.class.toString(), "Failed to get input stream for cert.");
                return null;
            }
            serverCert = new BufferedInputStream(inputStream);
            serverCert.mark(Integer.MAX_VALUE);
            serverCert.reset();
            return serverCert;
        } catch (Exception e) {
            Log.d(WebService.class.toString(), "Failed to read cert.");
        }
        return null;
    }

    public static File certFile(Context context) {
        return new File(context.getCacheDir(), "server.crt");
    }
}


