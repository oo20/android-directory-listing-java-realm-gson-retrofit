package com.example.directorylisting.helpers;

import android.net.Uri;

import com.example.directorylisting.shared.AppManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;

/**
 * Created by Michael Steele on 7/19/17.
 * Copyright © 2017 Michael Steele. All rights reserved.
 */

public class StringHelper {

    public static String identifierToText(String input) {

        String lowerInput = input.toLowerCase().replace("_", "");
        String[] words = lowerInput.split(" ");
        String output = "";

        if (words.length == 0 || (words.length == 1 && words[0].isEmpty())) {
            return output;
        }

        for (String word : words) {

            char upCase = Character.toUpperCase(word.charAt(0));
            output = output + new StringBuilder(word.substring(1)).insert(0, upCase);
        }

        return output;
    }

    public static String streamToText(InputStream inputStream) throws IOException {
        if (inputStream == null) {
            return "";
        }

        Writer writer = new StringWriter();

        char[] buffer = new char[1024];

        Reader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"));
        int count = 0;
        while ((count = reader.read(buffer)) != -1) {
            writer.write(buffer, 0, count);
        }
        return writer.toString();
    }

    public static String getResourcePath(int resource) {
        return Uri.parse("android.resource://" + AppManager.shared.packageName + "/" + resource).toString();
    }

}
