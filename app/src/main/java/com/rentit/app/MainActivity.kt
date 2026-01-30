package com.rentit.app

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rentit.app.retrofit.RegionsSingelton

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        RegionsSingelton.getRegionsFromRemoteApi()
    }
}

