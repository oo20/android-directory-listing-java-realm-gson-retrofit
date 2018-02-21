/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.auth

import android.content.Context
import android.util.Log

import java.io.BufferedInputStream
import java.io.InputStream
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.Certificate
import java.security.cert.CertificateFactory
import java.security.cert.X509Certificate

import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.TrustManagerFactory

object SSLUtils {

    fun getSslContextForCertificateFile(context: Context, trustManagers: Array<TrustManager>?, keyStore: KeyStore): SSLContext {
        try {
            val sslContext = SSLContext.getInstance("SSL")
            if (sslContext == null && trustManagers == null) {
                val trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
                trustManagerFactory.init(keyStore)
                sslContext!!.init(null, trustManagerFactory.trustManagers, SecureRandom())
                return sslContext
            }
            sslContext!!.init(null, trustManagers, SecureRandom())
            return sslContext
        } catch (e: Exception) {
            val errorText = "Failed to create SSLContext for certificate from assets."
            Log.e(SSLContext::class.java.toString(), errorText, e)
            throw RuntimeException(errorText)
        }

    }

    fun getKeyStore(context: Context, certificate: Certificate): KeyStore {
        var keyStore: KeyStore? = null
        try {
            val keyStoreType = KeyStore.getDefaultType()
            keyStore = KeyStore.getInstance(keyStoreType)
            keyStore!!.load(null, null)
            keyStore.setCertificateEntry("ca", certificate)
        } catch (e: Exception) {
            val errorText = "Failed to get keystore."
            Log.e(SSLContext::class.java.toString(), errorText, e)
            throw RuntimeException(errorText)
        }

        return keyStore
    }

    fun getBufferedInputStreamFromResource(context: Context, id: Int?): BufferedInputStream? {
        var bufferedInputStream: BufferedInputStream? = null
        var inputStream: InputStream? = null
        try {
            try {
                inputStream = context.resources.openRawResource(id!!)
                bufferedInputStream = BufferedInputStream(inputStream!!)
                bufferedInputStream.mark(Integer.MAX_VALUE)
                bufferedInputStream.reset()
            } finally {
                inputStream!!.close()
            }
        } catch (e: Exception) {
            val errorText = "Failed to get certificate from resource."
            Log.e(SSLContext::class.java.toString(), errorText, e)
            throw RuntimeException(errorText)
        }

        return bufferedInputStream
    }

    fun getX509Certificate(context: Context, id: Int?): X509Certificate? {
        var certificate: X509Certificate? = null
        try {
            certificate = getX509Certificate(context, getBufferedInputStreamFromResource(context, id))
        } catch (e: Exception) {
            val errorText = "Failed to get certificate from resource."
            Log.e(SSLContext::class.java.toString(), errorText, e)
            throw RuntimeException(errorText)
        }

        return certificate
    }

    fun getX509Certificate(context: Context, bufferedInputStream: BufferedInputStream?): X509Certificate {
        if (bufferedInputStream == null) {
            val errorText = "Input stream null."
            Log.e(SSLContext::class.java.toString(), errorText)
            throw RuntimeException(errorText)
        }

        var ca: Certificate? = null
        try {
            val cf = CertificateFactory.getInstance("X.509")
            bufferedInputStream.reset()
            ca = cf.generateCertificate(bufferedInputStream)
            bufferedInputStream.reset()
            Log.d(SSLContext::class.java.toString(), "ca=" + (ca as X509Certificate).subjectDN)
        } catch (e: Exception) {
            val errorText = "Failed to get certificate."
            Log.e(SSLContext::class.java.toString(), errorText, e)
            throw RuntimeException(errorText)
        }

        return ca
    }

}
