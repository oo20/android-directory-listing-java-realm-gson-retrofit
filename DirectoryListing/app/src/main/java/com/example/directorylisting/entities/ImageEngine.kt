/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.entities

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log

import com.bumptech.glide.Glide
import com.bumptech.glide.Priority
import com.bumptech.glide.load.Key
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.load.resource.drawable.GlideDrawable
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.example.directorylisting.application.R


class ImageEngine {

    interface ImageEngineInterface {
        fun finished(id: String, resource: Drawable)
    }

    fun loadImage(context: Context, tag: String, key: Key, url: String, size: Int?, imageEngineInterface: ImageEngineInterface) {

        try {

            var tempSize = size?: return

            // Glide 3
            Glide.with(context)
                    .load(GlideUrl(url))
                    .placeholder(R.drawable.spinner)
                    .override(tempSize, size)
                    .signature(key)
                    .diskCacheStrategy(DiskCacheStrategy.SOURCE)
                    .priority(Priority.HIGH)
                    .centerCrop()
                    .listener(object : RequestListener<GlideUrl, GlideDrawable> {
                        override fun onException(e: Exception, model: GlideUrl, target: Target<GlideDrawable>, isFirstResource: Boolean): Boolean {
                            Log.d(ImageEngine::class.java.toString(), "Glide Error: " + e.toString())
                            return false
                        }

                        override fun onResourceReady(resource: GlideDrawable, model: GlideUrl, target: Target<GlideDrawable>, isFromMemoryCache: Boolean, isFirstResource: Boolean): Boolean {
                            imageEngineInterface.finished(tag, resource)
                            return false
                        }
                    })
                    .preload()

        } catch (e: Exception) {
            Log.d(ImageEngine::class.java.toString(), "Error: " + e.toString())
        }

        /*
        // Glide 4
        Glide.with(context)
                    //.load(new GlideUrl(url, auth))
                    .load(url)
                    .apply(RequestOptions.placeholderOf(R.drawable.spinner))
                    .apply(RequestOptions.overrideOf(size, size))
                    .apply(RequestOptions.signatureOf(key))
                    .apply(RequestOptions.diskCacheStrategyOf(DiskCacheStrategy.AUTOMATIC))
                    .apply(RequestOptions.priorityOf(Priority.HIGH))
                    .apply(RequestOptions.centerCropTransform())
                    .listener(new RequestListener<Drawable>() {
                        @Override
                        public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                            imageEngineInterface.finished(tag, resource);
                            return false;
                        }

                        @Override
                        public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                            return false;
                        }
                    }).preload();

        } catch (Exception e) {
            Log.d(ImageEngine.class.toString(), "Error: " + e.toString());
        }*/

        /*
        // Vanilla / No 3rd-party library / No cache.
        AppManager.shared.webService.downloadFile(url, new WebService.BasicSuccessObjectFailureInterface() {
            @Override
            public void onResponse(InputStream inputStream) {
                Drawable drawable = Drawable.createFromStream(inputStream, url);
                imageEngineInterface.finished(tag, drawable);
            }

            @Override
            public void onFailure() {

            }
        });
        */

    }
}
