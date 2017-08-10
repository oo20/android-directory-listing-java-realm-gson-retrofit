/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.api;

import okhttp3.OkHttpClient;

public class WebServiceDetails {
    public WebServiceInterface service = null;
    public OkHttpClient client = null;
    public okhttp3.Call.Factory factory = null;
    public int cacheSize = 0;
}
