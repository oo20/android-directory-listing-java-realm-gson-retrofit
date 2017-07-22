package com.example.directorylisting.helpers;

/**
 * Created by Michael Steele on 7/19/17.
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
}
