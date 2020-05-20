package com.hyunki.aryoulearning2.ui.main

import com.hyunki.aryoulearning2.db.dao.CategoryDao
import com.hyunki.aryoulearning2.db.dao.CurrentCategoryDao
import com.hyunki.aryoulearning2.db.dao.ModelDao
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.CurrentCategory
import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.model.ModelResponse
import com.hyunki.aryoulearning2.network.main.MainApi

import java.util.ArrayList

import javax.inject.Inject
import javax.inject.Singleton

import io.reactivex.Observable
import io.reactivex.Single

@Singleton
class MainRepository @Inject
internal constructor(private val modelDao: ModelDao, private val categoryDao: CategoryDao, private val currentCategoryDao: CurrentCategoryDao, private val mainApi: MainApi) {

    internal val allCats: Single<List<Category>>
        get() = categoryDao.allCategories

    val currentCategory: Single<CurrentCategory>
        get() = currentCategoryDao.currentCategory

    val modelResponses: Observable<ArrayList<ModelResponse>>
        get() = mainApi.getModels()

    fun getModelsByCat(cat: String): Single<List<Model>> {
        return modelDao.getModelsByCat(cat)
    }

    internal fun insertModel(model: Model) {
        modelDao.insert(model)
    }

    internal fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    internal fun setCurrentCategory(category: CurrentCategory) {
        currentCategoryDao.insert(category)
    }

    internal fun clearEntireDatabase() {
        modelDao.deleteAll()
        categoryDao.deleteAll()
        currentCategoryDao.deleteAll()
    }
}
