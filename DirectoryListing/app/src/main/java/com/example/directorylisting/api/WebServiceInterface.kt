package com.example.directorylisting.api

import com.example.directorylisting.entities.Directory
import com.example.directorylisting.entities.Status
import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

interface WebServiceInterface {

    @get:GET("individuals/directory")
    val individuals: Call<Directory>

    @get:GET("status/")
    val status: Call<Status>

    @POST("individual/create")
    fun createIndividual(
            @Header("Content-Type") contentType: String,
            @Body individual: String
    ): Call<Directory>

    @PUT("individual/modify/{id}")
    fun modifyIndividual(
            @Header("Content-Type") contentType: String,
            @Path("id") id: String,
            @Body individual: String
    ): Call<Directory>

    @DELETE("individual/delete/{id}")
    fun deleteIndividual(@Path("id") id: String): Call<Directory>

    @Multipart
    @POST("tempFile/{id}")
    fun uploadFile(
            //@Header("Content-Type") String contentType,
            @Path("id") id: String,
            //@Part("description") RequestBody description,
            @Part tempFile: MultipartBody.Part): Call<ResponseBody>

    @GET
    fun downloadFile(@Url fileUrl: String): Call<ResponseBody>
}

