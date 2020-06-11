package com.hyunki.aryoulearning2.data

import androidx.lifecycle.LiveData
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import io.reactivex.Observable
import io.reactivex.Single
import kotlinx.coroutines.Deferred
import java.util.ArrayList

interface MainRepository {
    fun getAllCats(): LiveData<List<Category>>

    suspend fun getModelResponses(): List<ModelResponse>

    fun getModelsByCat(cat: String): LiveData<List<Model>>

    suspend fun insertModel(model: Model): Long

    suspend fun insertAllModels(vararg models: Model): List<Long>

    suspend fun insertCat(category: Category)

    fun checkSize(): Int

    fun clearEntireDatabase()
}