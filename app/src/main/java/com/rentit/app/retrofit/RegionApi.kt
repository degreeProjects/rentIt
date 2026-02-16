package com.rentit.app.retrofit

import retrofit2.Call
import retrofit2.http.GET

/**
 * RegionApi
 *
 * Retrofit API interface for fetching Israeli regions from government data API.
 */
interface RegionApi {
    // fetches distinct region names from Israeli government API
    @GET("3/action/datastore_search?resource_id=8f714b6f-c35c-4b40-a0e7-547b675eee0e&limit=20&offset=0&fields=region_name&distinct=true&sort=region_name&include_total=false")
    fun getRegions(): Call<RegionSearchResult>
}