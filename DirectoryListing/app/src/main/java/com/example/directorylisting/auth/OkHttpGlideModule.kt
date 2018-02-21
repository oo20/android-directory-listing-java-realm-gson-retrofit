/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.auth

import android.content.Context

import com.bumptech.glide.Glide
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.load.model.GlideUrl
import com.bumptech.glide.module.GlideModule
import com.example.directorylisting.shared.AppManager

import java.io.InputStream

class OkHttpGlideModule : GlideModule {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        builder.setDiskCache(InternalCacheDiskCacheFactory(context, AppManager.shared.webService.imageWebDetails!!.cacheSize))
    }

    override fun registerComponents(context: Context, glide: Glide) {
        glide.register(GlideUrl::class.java, InputStream::class.java, OkHttpUrlLoader.Factory(AppManager.shared.webService.webDetails!!.factory))
    }

}