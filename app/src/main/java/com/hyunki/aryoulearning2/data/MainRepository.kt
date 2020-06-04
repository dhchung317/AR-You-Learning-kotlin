package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import io.reactivex.Observable
import io.reactivex.Single
import java.util.ArrayList

interface MainRepository {
    fun getAllCats(): Single<List<Category>>

    fun getModelResponses(): Observable<ArrayList<ModelResponse>>

    fun getModelsByCat(cat: String): Single<List<Model>>

    fun insertModel(model: Model)

    fun insertCat(category: Category)

    fun clearEntireDatabase()
}