package com.rentit.app.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * RetrofitInstance
 *
 * Singleton that provides Retrofit client for API calls.
 * Configured with base URL and Gson converter for JSON parsing.
 */
object RetrofitInstance {
    private const val BASE_URL = "https://data.gov.il/api/"

    // lazily initialized Retrofit instance for regions API
    val regionsApi: RegionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL) // sets the base URL for API requests
            .addConverterFactory(GsonConverterFactory.create()) // uses Gson for JSON parsing
            .build() // builds the Retrofit instance
            .create(RegionApi::class.java) // creates the API interface
    }
}