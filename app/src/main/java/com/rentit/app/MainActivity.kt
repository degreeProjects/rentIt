package com.rentit.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rentit.app.retrofit.RegionsSingelton

/**
 * MainActivity
 *
 * Entry point activity for the app.
 * Handles login/register flow and fetches region data from API.
 */
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // fetch regions list from Israeli government API for apartment location selection
        RegionsSingelton.getRegionsFromRemoteApi()
    }
}

