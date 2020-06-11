package com.hyunki.aryoulearning2.data

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

    override fun getAllCats(): Single<List<Category>>{
        return categoryDao.allCategories
    }

    override suspend fun getModelResponses(): ArrayList<ModelResponse> = mainApi.getModels()


    override fun getModelsByCat(cat: String): Single<List<Model>> {
        return modelDao.getModelsByCat(cat)
    }

    override fun insertModel(model: Model) {
        modelDao.insert(model)
    }

    override fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    override fun clearEntireDatabase() {
        modelDao.deleteAll()
        categoryDao.deleteAll()
    }
}
