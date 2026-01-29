package com.rentit.app.retrofit

import com.google.gson.annotations.SerializedName

data class Region(
    @SerializedName("region_name")
    val regionName: String,
)

data class Result(
    val records: List<Region>,
)

data class RegionSearchResult(val result: Result)