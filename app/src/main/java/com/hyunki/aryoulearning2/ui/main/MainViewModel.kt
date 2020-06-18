package com.hyunki.aryoulearning2.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.util.DispatcherProvider
import javax.inject.Inject

//TODO fix/improve data structures/datamodeling logic
class MainViewModel @Inject
constructor(private val mainRepositoryImpl: MainRepository, private val defaultDispatcher: DispatcherProvider) : ViewModel() {

    private var wordHistory: List<CurrentWord> = ArrayList()

    fun getModelResponses() = liveData(defaultDispatcher.io()) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getModelResponses()
            saveResponseData(result)
            emit(MainState.Success.OnModelResponsesLoaded(result))
        } catch (exception: Exception) {
            emit(MainState.Error(exception.message.toString()))
        }
    }

    private suspend fun saveResponseData(response: List<ModelResponse>) {
        saveModels(getModelList(response))
        saveCategories(getCategories(response))
    }

    fun getModelsByCat(cat: String) = liveData(defaultDispatcher.io()) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getModelsByCat(cat)
            emit(MainState.Success.OnModelsLoaded(result))
        } catch (exception: Exception) {
            emit(MainState.Error(exception.message.toString()))
        }
    }

    fun getAllCats() = liveData(defaultDispatcher.io()) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getAllCats()
            emit(MainState.Success.OnCategoriesLoaded(result))
        } catch (exception: Exception) {
            emit(MainState.Error(exception.message.toString()))
        }
    }

    private fun getModelList(data: List<ModelResponse>): List<Model> {
        return parseModelsToSaveFromModelResponseData(data)
    }

    private fun getCategories(data: List<ModelResponse>): List<Category> {
        return parseCategoriesToSaveFromModelResponseData(data)
    }

    private fun parseCategoriesToSaveFromModelResponseData(modelResponses: List<ModelResponse>): List<Category> {
        val categories = arrayListOf<Category>()
        for (i in modelResponses.indices) {
            categories.add(Category(
                    modelResponses[i].category,
                    modelResponses[i].background
            ))
        }
        return categories
    }

    private fun parseModelsToSaveFromModelResponseData(modelResponses: List<ModelResponse>): List<Model> {
        val models = arrayListOf<Model>()
        for (i in modelResponses.indices) {
            models.addAll(modelResponses[i].list)
        }
        return models
    }

    private suspend fun saveCategories(categories: List<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
    }

    private suspend fun saveModels(models: List<Model>): List<Long> {
        return mainRepositoryImpl.insertAllModels(*models.toTypedArray())
    }

    fun getWordHistory(): List<CurrentWord> {
        return wordHistory
    }

    fun setWordHistory(wordHistory: List<CurrentWord>) {
        this.wordHistory = wordHistory
    }

    fun clearEntireDatabase() {
        mainRepositoryImpl.clearEntireDatabase()
    }

    override fun onCleared() {
        super.onCleared()
        //        clearEntireDatabase();
    }

    companion object {
        const val TAG = "mainviewmodel"
    }
}
