/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.auth


import android.content.Context
import com.example.directorylisting.api.WebService
import okhttp3.OkHttpClient
import java.security.cert.CertificateException
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager

object OkHttpUtils {

    fun getUnsafeOkHttpClientBuilder(context: Context, client: OkHttpClient): OkHttpClient.Builder {
        try {

            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                @Throws(CertificateException::class)
                override fun checkClientTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                @Throws(CertificateException::class)
                override fun checkServerTrusted(chain: Array<java.security.cert.X509Certificate>, authType: String) {
                }

                override fun getAcceptedIssuers(): Array<java.security.cert.X509Certificate> {
                    return arrayOf(SSLUtils.getX509Certificate(context, WebService.getCert(context))) //AppManager.shared.webService
                }
            })

            val sslContext = SSLUtils.getSslContextForCertificateFile(context, trustAllCerts, SSLUtils.getKeyStore(context, SSLUtils.getX509Certificate(context, WebService.getCert(context)))) //AppManager.shared.w
            val sslSocketFactory = sslContext.socketFactory

            val builder = client.newBuilder()
            builder.sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
            builder.hostnameVerifier { hostname, session -> true }

            return builder
        } catch (e: Exception) {
            throw RuntimeException(e)
        }

    }
}