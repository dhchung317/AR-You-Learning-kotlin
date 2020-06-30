package com.hyunki.aryoulearning2.data.network.main

import com.hyunki.aryoulearning2.data.db.model.ArModelResponse

import java.util.ArrayList

import retrofit2.http.GET

interface MainApi {

    @GET("dhchung317/1b9d814e3c82d643e91eff507c82513e/raw/ac1c2c7aebbc3a2a218efa5745e61a78796af4bf/ar-you-learning-2.json")
    suspend fun getModels(): ArrayList<ArModelResponse>
}
