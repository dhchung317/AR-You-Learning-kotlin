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

    override fun getAllCats(): LiveData<List<Category>>{
        return categoryDao.allCategories
    }

    override suspend fun getModelResponses(): List<ModelResponse> = mainApi.getModels()


    override fun getModelsByCat(cat: String): LiveData<List<Model>> {
//        Log.d("mainrepo", "getModelsByCat: " + cat)
//        val models = modelDao.getModelsByCat(cat)
//        Log.d("mainrepo", "getModelsByCat: " + models.size)
        return modelDao.getModelsByCat(cat)
    }

    override suspend fun insertModel(model: Model): Long {
        Log.d("mainrepo", "insertModel: " + model.name)
        return modelDao.insert(model)
    }

    override suspend fun insertAllModels(vararg models: Model): List<Long> {
        Log.d("mainrepo", "insertAllModels: " + models.size)
        return modelDao.insertAll(*models)
    }

    override suspend fun insertCat(category: Category) {
        categoryDao.insert(category)
    }

    override fun checkSize(): Int {
        return modelDao.checkSize()
    }

    override fun clearEntireDatabase() {
        modelDao.deleteAll()
        categoryDao.deleteAll()
    }
}
