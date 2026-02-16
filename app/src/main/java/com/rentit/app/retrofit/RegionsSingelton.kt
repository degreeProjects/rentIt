package com.rentit.app.retrofit

import android.util.Log
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * RegionsSingelton
 *
 * Singleton that manages and caches region data from Israeli government API.
 * Provides centralized access to region list throughout the app.
 */
object RegionsSingelton {
    // cached regions list
    private var _regionsSearchResult: List<String>? = null

    // public read-only access to regions list
    val regionsList: List<String> get() = _regionsSearchResult ?: listOf()

    // fetches regions from remote API asynchronously
    fun getRegionsFromRemoteApi() {
        val call = RetrofitInstance.regionsApi.getRegions()

        call.enqueue(object: Callback<RegionSearchResult> {
            // handles API response
            override fun onResponse(
                call: Call<RegionSearchResult>,
                response: Response<RegionSearchResult>
            ) {
                if (response.isSuccessful) {
                    // update cached regions list
                    _regionsSearchResult = response.body()?.result?.records?.map { it.regionName } ?: emptyList()
                    Log.i("TAG", regionsList.toString())
                } else {
                    Log.e("TAG", response.errorBody().toString())
                }
            }

            override fun onFailure(call: Call<RegionSearchResult>, t: Throwable) {
                Log.e("TAG", "Network call failed: ${t.message}")
                t.printStackTrace()
            }
        })
    }
}