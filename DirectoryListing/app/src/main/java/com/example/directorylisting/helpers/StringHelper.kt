package com.example.directorylisting.helpers

import android.net.Uri
import com.example.directorylisting.shared.AppManager
import java.io.*

/**
 * Created by Michael Steele on 7/19/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

object StringHelper {

    fun identifierToText(input: String): String {

        val lowerInput = input.toLowerCase().replace("_", "")
        val words = lowerInput.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        var output = ""

        if (words.size == 0 || words.size == 1 && words[0].isEmpty()) {
            return output
        }

        for (word in words) {

            val upCase = Character.toUpperCase(word[0])
            output = output + StringBuilder(word.substring(1)).insert(0, upCase)
        }

        return output
    }

    @Throws(IOException::class)
    fun streamToText(inputStream: InputStream?): String {
        if (inputStream == null) {
            return ""
        }

        val writer = StringWriter()

        val buffer = CharArray(1024)

        val reader = BufferedReader(InputStreamReader(inputStream, "UTF-8"))
        var count = 0
        while (1 == 1) { // kotlin doesn't support assignments in expressions
            count = reader.read(buffer)
            if (count == -1) break
            writer.write(buffer, 0, count)
        }
        return writer.toString()
    }

    fun getResourcePath(resource: Int): String {
        return Uri.parse("android.resource://" + AppManager.packageName + "/" + resource).toString()
    }

}
