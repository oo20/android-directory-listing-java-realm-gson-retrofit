/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.helpers

import android.content.Context
import android.util.Log
import com.example.directorylisting.api.WebService
import java.io.*

object FileHelper {

    fun cachedFile(context: Context, fileName: String): File {
        return File(context.cacheDir, fileName)
    }

    fun readFile(file: File, basicSuccessObjectFailureInterface: WebService.BasicSuccessObjectFailureInterface) {
        var inputStream: InputStream? = null
        try {
            inputStream = FileInputStream(file)

            basicSuccessObjectFailureInterface.onResponse(inputStream)
        } catch (e: Exception) {
            Log.e(WebService::class.java.toString(), "Failed to read cert.")
            e.printStackTrace()
        }

    }

    fun writeFile(context: Context, file: File, text: String, basicSuccessFailureInterface: WebService.BasicSuccessFailureInterface) {
        try {
            val outputStream = FileOutputStream(file)
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

}
