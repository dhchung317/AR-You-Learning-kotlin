package com.hyunki.aryoulearning2.data

import android.util.Log
import androidx.lifecycle.LiveData
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi
import io.reactivex.Observable
import io.reactivex.Single
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject
constructor(private val modelDao: ModelDao, private val categoryDao: CategoryDao, private val mainApi: MainApi):MainRepository {

    override suspend fun getAllCats(): List<Category>{
        return categoryDao.getAllCategories()
    }

    override suspend fun getModelResponses(): List<ModelResponse> = mainApi.getModels()

    override suspend fun getModelsByCat(cat: String): List<Model> {
        return modelDao.getModelsByCat(cat)
    }

    override suspend fun insertModel(model: Model): Long {
        return modelDao.insert(model)
    }

    override suspend fun insertAllModels(vararg models: Model): List<Long> {
        return modelDao.insertAll(*models)
    }

    override suspend fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    override fun clearEntireDatabase() {
        modelDao.deleteAll()
        categoryDao.deleteAll()
    }
}
