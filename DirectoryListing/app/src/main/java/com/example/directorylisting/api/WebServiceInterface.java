package com.example.directorylisting.api;

import com.example.directorylisting.entities.Directory;
import com.example.directorylisting.entities.Status;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Url;

/**
 * Created by Michael Steele on 3/22/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public interface WebServiceInterface {

    @GET("individuals/directory")
    Call<Directory> getIndividuals();

    @POST("individual/create")
    Call<Directory> createIndividual(
            @Header("Content-Type") String contentType,
            @Body String individual
    );

    @PUT("individual/modify/{id}")
    Call<Directory> modifyIndividual(
            @Header("Content-Type") String contentType,
            @Path("id") String id,
            @Body String individual
    );

    @DELETE("individual/delete/{id}")
    Call<Directory> deleteIndividual(@Path("id") String id);

    @Multipart
    @POST("tempFile/{id}")
    Call<ResponseBody> uploadFile(
            //@Header("Content-Type") String contentType,
            @Path("id") String id,
            //@Part("description") RequestBody description,
            @Part MultipartBody.Part tempFile);

    @GET
    Call<ResponseBody> downloadFile(@Url String fileUrl);

    @GET("status/")
    Call<Status> getStatus();
}

