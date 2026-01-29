package com.rentit.app.retrofit

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * A Singleton object used to manage and cache region data fetched from a remote API.
 * This ensures that region data is centralized and accessible throughout the app.
 */
object RegionsSingelton {

    // Internal private storage for the regions to prevent direct external modification.
    private var _regionsSearchResult: List<String>? = null

    /**
     * A public read-only property that provides the list of regions.
     * Returns an empty list if the data hasn't been fetched yet.
     */
    val regionsList: List<String> get() = _regionsSearchResult ?: listOf()

    /**
     * Initiates an asynchronous request to fetch region data.
     * It uses Retrofit's [enqueue] to handle the request on a background thread
     * and provides callbacks for success or failure.
     */
    fun getRegionsFromRemoteApi() {
        val call = RetrofitInstance.regionsApi.getRegions()

        // Executing the call asynchronously
        call.enqueue(object: Callback<RegionSearchResult> {

            /**
             * Invoked when a response is received.
             * Note: This does not guarantee the request was successful (e.g., 404 or 500 errors).
             */
            override fun onResponse(
                call: Call<RegionSearchResult>,
                response: Response<RegionSearchResult>
            ) {
                if (response.isSuccessful) {
                    // Extract the list of records and map them to a simple list of names (Strings)
                    _regionsSearchResult = response.body()?.result?.records?.map { it.regionName } ?: emptyList()

                    Log.i("TAG", regionsList.toString())
                } else {
                    Log.e("TAG", response.errorBody().toString())
                }
            }

            /**
             * Invoked when an exception occurred talking to the server
             * or when an unexpected exception occurred creating the request or processing the response.
             */
            override fun onFailure(call: Call<RegionSearchResult>, t: Throwable) {
                Log.e("TAG", "Network call failed: ${t.message}")
                t.printStackTrace()
            }
        })
    }
}