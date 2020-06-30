package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ArModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.data.db.model.ArModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MainRepositoryImpl @Inject
constructor(private val arModelDao: ArModelDao, private val categoryDao: CategoryDao, private val mainApi: MainApi):MainRepository {

    override suspend fun getAllCats(): List<Category>{
        return categoryDao.getAllCategories()
    }

    override suspend fun getModelResponses(): List<ArModelResponse> = mainApi.getModels()

    override suspend fun getModelsByCat(cat: String): List<ArModel> {
        return arModelDao.getModelsByCat(cat)
    }

    override suspend fun insertModel(arModel: ArModel): Long {
        return arModelDao.insert(arModel)
    }

    override suspend fun insertAllModels(vararg arModels: ArModel): List<Long> {
        return arModelDao.insertAll(*arModels)
    }

    override suspend fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    override fun clearEntireDatabase() {
        arModelDao.deleteAll()
        categoryDao.deleteAll()
    }
}
