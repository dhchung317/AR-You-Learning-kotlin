package com.hyunki.aryoulearning2.network.main

import com.hyunki.aryoulearning2.db.model.ModelResponse

import java.util.ArrayList

import io.reactivex.Observable
import retrofit2.http.GET

interface MainApi {

    @GET("kelveenfabian/75380ae0e467f513762454bbe49a6c2e/raw/7c745364e690cb292ee13eae0a157df6323e3a19/category.json")
    fun getModels(): Observable<ArrayList<ModelResponse>>
}
