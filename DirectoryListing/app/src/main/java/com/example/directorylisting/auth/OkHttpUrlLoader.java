/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.auth;

import android.content.Context;

import com.bumptech.glide.integration.okhttp3.OkHttpStreamFetcher;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.model.GenericLoaderFactory;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.load.model.ModelLoader;
import com.bumptech.glide.load.model.ModelLoaderFactory;
import com.bumptech.glide.load.model.stream.StreamModelLoader;

import java.io.InputStream;

import okhttp3.Call;
import okhttp3.OkHttpClient;

// kotlin issues have duplicate signatures with internalClient (keep in java for now)
public class OkHttpUrlLoader implements StreamModelLoader<GlideUrl> {

    private final Call.Factory client;

    public OkHttpUrlLoader(Call.Factory client) {
        this.client = client;
    }

    @Override
    public DataFetcher<InputStream> getResourceFetcher(GlideUrl model, int width, int height) {
        return new OkHttpStreamFetcher(client, model);
    }

    public static class Factory implements ModelLoaderFactory<GlideUrl, InputStream> {
        private static volatile Call.Factory internalClient;
        private Call.Factory client;


        public Factory() {
            this(getInternalClient());
        }


        public Factory(Call.Factory client) {
            this.client = client;
        }

        private static Call.Factory getInternalClient() {
            if (internalClient == null) {
                synchronized (com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader.Factory.class) {
                    if (internalClient == null) {
                        internalClient = new OkHttpClient();
                    }
                }
            }
            return internalClient;
        }

        @Override
        public ModelLoader<GlideUrl, InputStream> build(Context context, GenericLoaderFactory factories) {
            return new com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader(client);
        }

        @Override
        public void teardown() {
        }
    }
}