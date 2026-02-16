package com.rentit.app.retrofit

import com.google.gson.annotations.SerializedName

/**
 * Region data classes for parsing API response.
 * Maps JSON from Israeli government API to Kotlin objects.
 */

// represents a single region from the API
data class Region(
    @SerializedName("region_name")
    val regionName: String,
)

// wrapper for list of regions
data class Result(
    val records: List<Region>,
)

// root response object from API
data class RegionSearchResult(val result: Result)