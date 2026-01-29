package com.rentit.app.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * A Singleton object ensures that only one instance of the Retrofit client exists
 */
object RetrofitInstance {
    private const val BASE_URL = "https://data.gov.il/api/"

    /**
     * The implementation of the [RegionApi] interface.
     * * 'by lazy' ensures the Retrofit instance is not initialized until the first time
     * [regionsApi] is actually accessed, saving resources during app startup.
     */
    val regionsApi: RegionApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)

            // Adding a converter factory to handle the mapping of JSON data
            // from the server into Kotlin Data Classes using the GSON library.
            .addConverterFactory(GsonConverterFactory.create())

            // Finalizing the configuration
            .build()

            // Creating the implementation of the API service based on the interface
            .create(RegionApi::class.java)
    }
}