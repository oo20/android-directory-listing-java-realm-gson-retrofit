/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.helpers;

import android.content.Context;
import android.util.Log;

import com.example.directorylisting.api.WebService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class FileHelper {

    public static File cachedFile(Context context, String fileName) {
        return new File(context.getCacheDir(), fileName);
    }

    public static void readFile(File file, WebService.BasicSuccessObjectFailureInterface basicSuccessObjectFailureInterface) {
        InputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);

            basicSuccessObjectFailureInterface.onResponse(inputStream);
        } catch (Exception e) {
            Log.e(WebService.class.toString(), "Failed to read cert.");
            e.printStackTrace();
        }
    }

    public static void writeFile(Context context, File file, String text, WebService.BasicSuccessFailureInterface basicSuccessFailureInterface) {
        try {
            OutputStream outputStream = new FileOutputStream(file);
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

}
