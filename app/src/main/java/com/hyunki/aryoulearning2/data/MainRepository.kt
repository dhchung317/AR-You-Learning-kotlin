package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.CurrentCategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.CurrentCategory
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.data.network.main.MainApi

import java.util.ArrayList

import javax.inject.Inject
import javax.inject.Singleton

import io.reactivex.Observable
import io.reactivex.Single

@Singleton
class MainRepository @Inject
constructor(private val modelDao: ModelDao, private val categoryDao: CategoryDao, private val currentCategoryDao: CurrentCategoryDao, private val mainApi: MainApi) {

    val allCats: Single<List<Category>>
        get() = categoryDao.allCategories

//    val currentCategory: Single<CurrentCategory>
//        get() = currentCategoryDao.currentCategory

    val modelResponses: Observable<ArrayList<ModelResponse>>
        get() = mainApi.getModels()

    fun getModelsByCat(cat: String): Single<List<Model>> {
        return modelDao.getModelsByCat(cat)
    }

    fun insertModel(model: Model) {
        modelDao.insert(model)
    }

    fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    fun setCurrentCategory(category: CurrentCategory) {
        currentCategoryDao.insert(category)
    }

    fun clearEntireDatabase() {
        modelDao.deleteAll()
        categoryDao.deleteAll()
        currentCategoryDao.deleteAll()
    }
}
