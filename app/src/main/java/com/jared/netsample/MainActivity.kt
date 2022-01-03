package com.jared.netsample

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.jared.netsample.remote.adapter.NetworkHeaderAdapterFactory
import com.jared.netsample.remote.converter.MoshiConverterFactory
import com.jared.netsample.remote.moshi.MoshiArrayListJsonAdapter
import com.jared.netsample.sample.Api
import com.squareup.moshi.Moshi
import okhttp3.OkHttpClient
import retrofit2.Retrofit

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val r=Retrofit.Builder()
            .baseUrl("https://script.google.com/")
            .client(
                OkHttpClient.Builder().build())

            .addCallAdapterFactory(NetworkHeaderAdapterFactory())

            .addConverterFactory( MoshiConverterFactory.create(Moshi.Builder()
                .add(MoshiArrayListJsonAdapter.FACTORY)
                .build()))
            .build()
        r.create(Api::class.java)
    }
}