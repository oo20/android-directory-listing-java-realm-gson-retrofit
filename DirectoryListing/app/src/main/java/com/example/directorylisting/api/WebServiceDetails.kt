/*
 * Copyright © 2017 Michael Steele.
 * All rights reserved.
 */

package com.example.directorylisting.api

import okhttp3.OkHttpClient

class WebServiceDetails {
    var service: WebServiceInterface? = null
    var client: OkHttpClient? = null
    var factory: okhttp3.Call.Factory? = null
    var cacheSize = 0
}
