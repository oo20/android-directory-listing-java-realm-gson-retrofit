/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.auth;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.SecureRandom;
import java.security.cert.Certificate;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLUtils {

    public static SSLContext getSslContextForCertificateFile(Context context, TrustManager trustManagers[], KeyStore keyStore) {
        try {
            SSLContext sslContext = SSLContext.getInstance("SSL");
            if (sslContext == null && trustManagers == null) {
                TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init(keyStore);
                sslContext.init(null, trustManagerFactory.getTrustManagers(), new SecureRandom());
                return sslContext;
            }
            sslContext.init(null, trustManagers, new SecureRandom());
            return sslContext;
        } catch (Exception e) {
            String errorText = "Failed to create SSLContext for certificate from assets.";
            Log.e(SSLContext.class.toString(), errorText, e);
            throw new RuntimeException(errorText);
        }
    }

    public static KeyStore getKeyStore(Context context, Certificate certificate) {
        KeyStore keyStore = null;
        try {
            String keyStoreType = KeyStore.getDefaultType();
            keyStore = KeyStore.getInstance(keyStoreType);
            keyStore.load(null, null);
            keyStore.setCertificateEntry("ca", certificate);
        } catch (Exception e) {
            String errorText = "Failed to get keystore.";
            Log.e(SSLContext.class.toString(), errorText, e);
            throw new RuntimeException(errorText);
        }
        return keyStore;
    }

    public static BufferedInputStream getBufferedInputStreamFromResource(Context context, Integer id) {
        BufferedInputStream bufferedInputStream = null;
        InputStream inputStream = null;
        try {
            try {
                inputStream = context.getResources().openRawResource(id);
                bufferedInputStream = new BufferedInputStream(inputStream);
                bufferedInputStream.mark(Integer.MAX_VALUE);
                bufferedInputStream.reset();
            } finally {
                inputStream.close();
            }
        } catch (Exception e) {
            String errorText = "Failed to get certificate from resource.";
            Log.e(SSLContext.class.toString(), errorText, e);
            throw new RuntimeException(errorText);
        }
        return bufferedInputStream;
    }

    public static X509Certificate getX509Certificate(Context context, Integer id) {
        X509Certificate certificate = null;
        try {
            certificate = getX509Certificate(context, getBufferedInputStreamFromResource(context, id));
        } catch (Exception e){
            String errorText = "Failed to get certificate from resource.";
            Log.e(SSLContext.class.toString(), errorText, e);
            throw new RuntimeException(errorText);
        }
        return certificate;
    }

    public static X509Certificate getX509Certificate(Context context, BufferedInputStream bufferedInputStream) {
        if (bufferedInputStream == null) {
            String errorText = "Input stream null.";
            Log.e(SSLContext.class.toString(), errorText);
            throw new RuntimeException(errorText);
        }

        Certificate ca = null;
        try {
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            bufferedInputStream.reset();
            ca = cf.generateCertificate(bufferedInputStream);
            bufferedInputStream.reset();
            Log.d(SSLContext.class.toString(), "ca=" + ((X509Certificate) ca).getSubjectDN());
        } catch (Exception e) {
            String errorText = "Failed to get certificate.";
            Log.e(SSLContext.class.toString(), errorText, e);
            throw new RuntimeException(errorText);
        }
        return ((X509Certificate) ca);
    }

}
