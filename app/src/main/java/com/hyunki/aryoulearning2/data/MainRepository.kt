package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.data.db.model.ArModelResponse

interface MainRepository {
    suspend fun getAllCats(): List<Category>

    suspend fun getModelResponses(): List<ArModelResponse>

    suspend fun getModelsByCat(cat: String): List<ArModel>

    suspend fun insertModel(arModel: ArModel): Long

    suspend fun insertAllModels(vararg arModels: ArModel): List<Long>

    suspend fun insertCat(category: Category)

    fun clearEntireDatabase()
}