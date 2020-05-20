package com.hyunki.aryoulearning2.network

import com.hyunki.aryoulearning2.network.main.MainApi

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitSingleton {
    private val BASEURL = "https://gist.githubusercontent.com/"
    private var instance: Retrofit? = null

    val service: MainApi
        get() = getInstance().create(MainApi::class.java)


    private fun getInstance(): Retrofit {
        if (instance == null) {
            instance = Retrofit.Builder()
                    .baseUrl(BASEURL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                    .build()
        }
        return instance
    }
}
