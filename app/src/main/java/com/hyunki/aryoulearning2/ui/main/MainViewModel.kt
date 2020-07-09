package com.hyunki.aryoulearning2.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.data.db.model.ArModelResponse
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.util.DispatcherProvider
import javax.inject.Inject

//TODO fix/improve data structures/datamodeling logic
class MainViewModel @Inject
constructor(private val mainRepositoryImpl: MainRepository, private val defaultDispatcher: DispatcherProvider) : ViewModel() {

    private lateinit var wordHistory: List<CurrentWord>

    fun getModelResponses() = liveData(defaultDispatcher.io()) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getModelResponses()
            if(result.isEmpty()){
                emit(MainState.Error("results returned empty, check network call"))
            }else{
                saveResponseData(result)
                emit(MainState.Success.OnModelResponsesLoaded(result))
            }
        } catch (exception: Exception) {
            emit(MainState.Error(exception.message.toString()))
        }
    }

    private suspend fun saveResponseData(response: List<ArModelResponse>) {
        saveModels(getParsedModelList(response))
        saveCategories(getParsedCategories(response))
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

    private fun getParsedModelList(data: List<ArModelResponse>): List<ArModel> {
        return parseModelsToSaveFromModelResponseData(data)
    }

    private fun getParsedCategories(data: List<ArModelResponse>): List<Category> {
        return parseCategoriesToSaveFromModelResponseData(data)
    }

    private fun parseCategoriesToSaveFromModelResponseData(arModelResponses: List<ArModelResponse>): List<Category> {
        val categories = arrayListOf<Category>()
        for (i in arModelResponses.indices) {
            categories.add(Category(
                    arModelResponses[i].category,
                    arModelResponses[i].background
            ))
        }
        return categories
    }

    private fun parseModelsToSaveFromModelResponseData(arModelResponses: List<ArModelResponse>): List<ArModel> {
        val models = arrayListOf<ArModel>()
        for (i in arModelResponses.indices) {
            models.addAll(arModelResponses[i].list)
        }
        return models
    }

    private suspend fun saveCategories(categories: List<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
    }

    private suspend fun saveModels(arModels: List<ArModel>): List<Long> {
        return mainRepositoryImpl.insertAllModels(*arModels.toTypedArray())
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

    companion object {
        const val TAG = "mainviewmodel"
    }
}
