package com.example.directorylisting.api

import android.content.Context
import android.util.Log
import com.burgstaller.okhttp.AuthenticationCacheInterceptor
import com.burgstaller.okhttp.CachingAuthenticatorDecorator
import com.burgstaller.okhttp.DispatchingAuthenticator
import com.burgstaller.okhttp.basic.BasicAuthenticator
import com.burgstaller.okhttp.digest.CachingAuthenticator
import com.burgstaller.okhttp.digest.Credentials
import com.burgstaller.okhttp.digest.DigestAuthenticator
import com.example.directorylisting.auth.OkHttpUtils
import com.example.directorylisting.auth.SSLUtils
import com.example.directorylisting.entities.Directory
import com.example.directorylisting.entities.Individual
import com.example.directorylisting.entities.Status
import com.example.directorylisting.helpers.StringHelper
import com.example.directorylisting.shared.AppManager
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import io.realm.Realm
import io.realm.RealmModel
import io.realm.RealmResults
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.io.*
import java.util.*
import java.util.concurrent.ConcurrentHashMap

//import Status;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

class WebService {

    var webDetails: WebServiceDetails? = null
    var imageWebDetails: WebServiceDetails? = null
    var certWebDetails: WebServiceDetails? = null

    private val webService: WebServiceInterface?
        get() = webDetails?.service

    private val individuals: Call<Directory>?
        get() = webService?.individuals

    interface IndividualsInterface {
        fun onResponse(individuals: List<Individual>)
        fun onFailure()
    }

    interface IndividualInterface {
        fun onResponse(individual: Individual)
        fun onFailure()
    }

    interface BasicSuccessObjectFailureInterface {
        fun onResponse(inputStream: InputStream)
        fun onFailure()
    }

    interface BasicSuccessFailureInterface {
        fun onSuccess()
        fun onFailure()
    }

    fun checkStatus(webServiceDetails: WebServiceDetails?, basicSuccessFailureInterface: BasicSuccessFailureInterface) {
        webServiceDetails?.service?.status?.enqueue(object : Callback<Status> {
            override fun onResponse(call: Call<Status>, response: Response<Status>) {
                val status = response.body()
                if (status == null || status.server == null || status.server == "ok" == false) {
                    basicSuccessFailureInterface.onFailure()
                    return
                }
                basicSuccessFailureInterface.onSuccess()
            }

            override fun onFailure(call: Call<Status>, t: Throwable) {
                basicSuccessFailureInterface.onFailure()
            }
        })
    }

    fun getIndividuals(individualsInterface: IndividualsInterface) {
        clear(Individual::class.java)
        fetchIndividuals(individualsInterface)
    }

    fun fetchIndividuals(individualsInterface: IndividualsInterface) {

        val realm = Realm.getDefaultInstance()

        val storedIndividuals = realm.where(Individual::class.java).findAll()

        if (storedIndividuals.size > 0) {
            individualsInterface.onResponse(WebService.convertResultsToList(storedIndividuals))
            return
        }

        individuals?.enqueue(object : Callback<Directory> {
            override fun onResponse(call: Call<Directory>, response: Response<Directory>) {
                Log.d(WebService::class.java.toString(), "Response:" + response.toString())

                val directory = response.body()

                val individuals = ArrayList<Individual>()

                val realm = Realm.getDefaultInstance()

                if (response.code() != 200 || directory.individuals == null) {
                    Log.d(WebService::class.java.toString(), "Response code: " + response.code().toString() + "  Also: Individual list is null. Failure.")

                    individualsInterface.onFailure()
                    return
                }

                realm.beginTransaction()
                for (individual in directory.individuals) {
                    individuals.add(individual)
                    realm.insertOrUpdate(individual.safeForRealm())
                }
                realm.commitTransaction()

                individualsInterface.onResponse(individuals)
            }

            override fun onFailure(call: Call<Directory>, t: Throwable) {
                Log.d(WebService::class.java.toString(), "getIndividuals Failure: " + t)

                individualsInterface.onFailure()
            }
        })
    }

    private fun createIndividual(individual: Individual): Call<Directory>? {
        val realm = Realm.getDefaultInstance()

        val directory = Directory().clear()
        directory.individuals.add(individual)
        return webService?.createIndividual("application/json", Gson().toJson(directory))
    }

    fun createIndividual(individual: Individual, individualInterface: IndividualInterface) {
        assert(individual.id.isEmpty() == true) // Want to maintain calling function distinction in the app
        saveIndividual(individual, individualInterface)
    }

    private fun saveIndividual(individual: Individual, individualInterface: IndividualInterface) {

        val responseCallback = object : Callback<Directory> {
            override fun onResponse(call: Call<Directory>, response: Response<Directory>) {
                Log.d(WebService::class.java.toString(), "Response:" + response.toString())

                val directory = response.body()

                val individuals = ArrayList<Individual>()

                if (directory.individuals.size <= 0) {
                    Log.d(WebService::class.java.toString(), "Failed to save individual properly.  Individual not returned from server.")

                    individualInterface.onFailure()
                    return
                }

                val realm = Realm.getDefaultInstance()

                realm.beginTransaction()
                for (listedIndividual in directory.individuals) {
                    individuals.add(listedIndividual)
                    realm.insertOrUpdate(listedIndividual.safeForRealm())
                }
                realm.commitTransaction()

                individualInterface.onResponse(individuals[0].safeForRealm())
            }

            override fun onFailure(call: Call<Directory>, t: Throwable) {
                Log.d(WebService::class.java.toString(), "Failed to save individual.")

                individualInterface.onFailure()
            }
        }

        if (individual.id.isEmpty()) {
            Log.d(WebService::class.java.toString(), "Creating Individual")

            AppManager.shared.webService.createIndividual(individual)?.enqueue(responseCallback)
        } else {
            Log.d(WebService::class.java.toString(), "Modifying Individual: " + individual.id)

            AppManager.shared.webService.modifyIndividual(individual.id, individual)?.enqueue(responseCallback)
        }

    }

    private fun modifyIndividual(id: String, individual: Individual): Call<Directory>? {
        val directory = Directory().clear()
        directory.individuals.add(individual)
        return webService?.modifyIndividual("application/json", id, Gson().toJson(directory))
    }

    fun modifyIndividual(id: String, individual: Individual, individualInterface: IndividualInterface) {
        assert(individual.id.isEmpty() == false) // Want to maintain calling function distinction in the app
        saveIndividual(individual, individualInterface)
    }

    fun deleteIndividual(id: String, deleteIndividualInterface: BasicSuccessFailureInterface) {
        webService?.deleteIndividual(id)?.enqueue(object : Callback<Directory> {
            override fun onResponse(call: Call<Directory>, response: Response<Directory>) {
                deleteIndividualInterface.onSuccess()
            }

            override fun onFailure(call: Call<Directory>, t: Throwable) {
                deleteIndividualInterface.onFailure()
            }
        })
    }

    fun uploadFile(id: String, fileData: String, uploadFileInterface: BasicSuccessFailureInterface) {
        webService?.uploadFile(id, MultipartBody.Part.createFormData("tempFile", fileData))?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(WebService::class.java.toString(), "Saving photo response:" + response.toString())

                uploadFileInterface.onSuccess()
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.d(WebService::class.java.toString(), "Failed to save photo.")

                uploadFileInterface.onFailure()
            }
        })
    }

    fun downloadFile(downloadServiceDetails: WebServiceDetails, url: String, downloadFileInterface: BasicSuccessObjectFailureInterface) {
        downloadServiceDetails.service?.downloadFile(url)?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(WebService::class.java.toString(), "Downloaded file: " + url)
                downloadFileInterface.onResponse(response.body().byteStream() as InputStream)
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(WebService::class.java.toString(), "Failed to download file: " + url)
                downloadFileInterface.onFailure()
            }
        })
    }

    fun downloadSaveFile(context: Context, downloadServiceDetails: WebServiceDetails, url: String, basicSuccessFailureInterface: BasicSuccessFailureInterface) {
        downloadServiceDetails.service?.downloadFile(url)?.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                Log.d(WebService::class.java.toString(), "Downloaded file: " + url)

                var text: String? = null
                try {
                    text = String(response.body().bytes())
                } catch (e: IOException) {
                    Log.e(WebService::class.java.toString(), "Failed to convert cert to string: " + url)
                    basicSuccessFailureInterface.onFailure()
                    e.printStackTrace()
                    return
                }

                writeCert(context, text, object : BasicSuccessFailureInterface {
                    override fun onSuccess() {
                        Log.e(WebService::class.java.toString(), "Wrote downloaded file: " + url)
                        basicSuccessFailureInterface.onSuccess()
                    }

                    override fun onFailure() {
                        Log.e(WebService::class.java.toString(), "Failed to write downloaded file: " + url)
                        basicSuccessFailureInterface.onFailure()
                    }
                })
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e(WebService::class.java.toString(), "Failed to download file: " + url)
                basicSuccessFailureInterface.onFailure()
            }
        })
    }


    fun clear(clazz: Class<out RealmModel>) {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        realm.delete(clazz)
        realm.commitTransaction()
    }

    fun clearData() {
        val realm = Realm.getDefaultInstance()

        realm.beginTransaction()
        realm.deleteAll()
        realm.commitTransaction()
    }

    fun initWebService(context: Context, onlyValidCert: Boolean, cacheSize: Int): WebServiceDetails {
        val details = WebServiceDetails()

        details.cacheSize = cacheSize

        val gson = GsonBuilder()
                .create()

        val httpLoggingInterceptor = HttpLoggingInterceptor(HttpLoggingInterceptor.Logger { message ->
            Log.d(WebService::class.java.toString(), "TODO: HttpLoggingInterceptor: " + message) // TODO: Implement the ability to start a login activity
        })
        httpLoggingInterceptor.level = HttpLoggingInterceptor.Level.BODY

        val credentials = Credentials(AppManager.shared.appUser, AppManager.shared.appPassword)

        val basicAuthenticator = BasicAuthenticator(credentials)
        val digestAuthenticator = DigestAuthenticator(credentials)

        val authenticator = DispatchingAuthenticator.Builder()
                .with("digest", digestAuthenticator)
                //.with("basic", basicAuthenticator) // Not needed currently.
                .build()

        val authCache = ConcurrentHashMap<String, CachingAuthenticator>()
        val okHttpClient = OkHttpClient()
        val okHttpClientBuilder = (if (onlyValidCert == true) OkHttpClient.Builder() else OkHttpUtils.getUnsafeOkHttpClientBuilder(context, okHttpClient))
                .authenticator(CachingAuthenticatorDecorator(authenticator, authCache))
                .addInterceptor(AuthenticationCacheInterceptor(authCache))
                .build()

        details.service = Retrofit.Builder()
                .baseUrl(AppManager.shared.baseURL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create(gson))
                .callFactory(okHttpClientBuilder)
                .build().create(WebServiceInterface::class.java)
        details.client = okHttpClient
        details.factory = okHttpClientBuilder

        return details
    }

    private fun initService(context: Context) {
        val onlyValidCert = !AppManager.shared.allowInvalidCert

        webDetails = initWebService(context, onlyValidCert, 0) // No cache for the API server side.  Using realm database instead.
        imageWebDetails = initWebService(context, onlyValidCert, AppManager.shared.imageCacheSize)
    }

    fun initService(context: Context, resourceId: Int, basicSuccessFailureInterface: BasicSuccessFailureInterface) {
        val onlyValidCert = !AppManager.shared.allowInvalidCert

        if (onlyValidCert == true) {
            initService(context)
            basicSuccessFailureInterface.onSuccess()
            return
        }

        certWebDetails = initWebService(context, true, 0)

        val serverCert = getCert(context)

        // Found cached cert
        if (serverCert != null) {
            initService(context)

            checkStatus(webDetails, object : BasicSuccessFailureInterface {
                override fun onSuccess() {
                    Log.d(WebService::class.java.toString(), "Using cached resource file.")
                    basicSuccessFailureInterface.onSuccess()
                }

                override fun onFailure() {
                    Log.d(WebService::class.java.toString(), "Bad certificate.")
                    getNewCert(context, resourceId, basicSuccessFailureInterface)
                }
            })

            return
        }

        getNewCert(context, resourceId, basicSuccessFailureInterface)
    }

    private fun getNewCert(context: Context, resourceId: Int, basicSuccessFailureInterface: BasicSuccessFailureInterface) {
        var serverCert: InputStream? = null

        // Get cert from resource
        if (resourceId > 0) {
            serverCert = SSLUtils.getBufferedInputStreamFromResource(context, resourceId)
            if (serverCert == null) {
                Log.d(WebService::class.java.toString(), "Failed to get buffered input stream from resource.")
                basicSuccessFailureInterface.onFailure()
                return
            }
            var text: String? = null
            try {
                text = StringHelper.streamToText(serverCert)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            if (text == null) {
                Log.d(WebService::class.java.toString(), "Failed to get buffered input stream from resource.")
                basicSuccessFailureInterface.onFailure()
                return
            }

            writeCert(context, text, object : BasicSuccessFailureInterface {
                override fun onSuccess() {
                    Log.e(WebService::class.java.toString(), "Wrote resource file.")
                    initService(context)
                    basicSuccessFailureInterface.onSuccess()
                }

                override fun onFailure() {
                    Log.e(WebService::class.java.toString(), "Failed to write resource file.")
                    basicSuccessFailureInterface.onFailure()
                }
            })

            return
        }

        // No resource cert, so downloading cert.

        val certDetails = certWebDetails?: return

        downloadSaveFile(context, certDetails, AppManager.shared.invalidCertURL, object : BasicSuccessFailureInterface {
            override fun onSuccess() {
                Log.d(WebService::class.java.toString(), "Downloaded server cert.")

                initService(context)

                basicSuccessFailureInterface.onSuccess()

            }

            override fun onFailure() {
                Log.d(WebService::class.java.toString(), "Failed to download server cert.")

                basicSuccessFailureInterface.onFailure()

            }
        })
    }

    companion object {

        fun <T : Individual> convertResultsToList(results: RealmResults<T>): List<T> {
            val realm = Realm.getDefaultInstance()

            val list = ArrayList<T>()
            for (result in results) {
                list.add(result.safeForRealm() as T)
            }
            return list
        }

        fun writeCert(context: Context, text: String?, basicSuccessFailureInterface: WebService.BasicSuccessFailureInterface) {
            try {
                val textToWrite = text?: ""

                val outputStream = FileOutputStream(certFile(context))
                val writer = OutputStreamWriter(outputStream)

                writer.write(text)

                writer.close()

                basicSuccessFailureInterface.onSuccess()
            } catch (e: Exception) {
                Log.e(WebService::class.java.toString(), "Failed to write downloaded file.")

                e.printStackTrace()

                basicSuccessFailureInterface.onFailure()
            }

        }

        fun getCertInputStream(context: Context): InputStream? {
            var targetStream: InputStream? = null
            val initialFile = certFile(context)
            try {
                targetStream = FileInputStream(initialFile)
                return targetStream
            } catch (e: FileNotFoundException) {
                Log.e(WebService::class.java.toString(), "Failed to get input stream for file.")
                e.printStackTrace()
            }

            return null
        }

        fun getCert(context: Context): BufferedInputStream? {
            var serverCert: BufferedInputStream? = null
            try {
                val inputStream = getCertInputStream(context)
                if (inputStream == null) {
                    Log.d(WebService::class.java.toString(), "Failed to get input stream for cert.")
                    return null
                }
                serverCert = BufferedInputStream(inputStream)
                serverCert.mark(Integer.MAX_VALUE)
                serverCert.reset()
                return serverCert
            } catch (e: Exception) {
                Log.d(WebService::class.java.toString(), "Failed to read cert.")
            }

            return null
        }

        fun certFile(context: Context): File {
            return File(context.cacheDir, "server.crt")
        }
    }
}


