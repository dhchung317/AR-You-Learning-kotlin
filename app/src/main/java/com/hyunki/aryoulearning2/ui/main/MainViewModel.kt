package com.hyunki.aryoulearning2.ui.main

import android.util.Log
import androidx.lifecycle.*
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.collections.ArrayList
//TODO fix/improve data structures/datamodeling logic
class MainViewModel @Inject
internal constructor(private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val modelResponsesData = MutableLiveData<MainState>()
    private val modelLiveData = MutableLiveData<MainState>()
    private val catLiveData = MutableLiveData<MainState>()
    private var wordHistory: List<CurrentWord> = ArrayList()

    fun getModelResponses() = liveData(Dispatchers.IO) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getModelResponses()
            emit(MainState.Success.OnModelResponsesLoaded(result))
            viewModelScope.launch(Dispatchers.Default) {
                saveResponseData(result)
            }
        } catch (exception: Exception) {
            emit(MainState.Error(exception.localizedMessage))
            Log.d(TAG, "getModelResponses: " + exception.cause)
        }
    }

    private suspend fun saveResponseData(response: List<ModelResponse>) {
        saveModels(getModelList(response))
        saveCategories(getCategories(response))
    }

    fun getModelsByCat(cat: String) = liveData(Dispatchers.IO) {
        emit(MainState.Loading)
        try {
            val result = mainRepositoryImpl.getModelsByCat(cat)
            emit(MainState.Success.OnModelsLoaded(result))
        } catch (exception: Exception) {
            emit(MainState.Error(exception.message.toString()))
        }
    }

    fun getAllCats() = liveData(Dispatchers.IO) {
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

    private suspend fun saveCategories(categories: List<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
    }

    private suspend fun saveModels(models: List<Model>): List<Long> {
        return mainRepositoryImpl.insertAllModels(*models.toTypedArray())
    }


    fun getModelLiveData(): LiveData<MainState> {
        return modelLiveData
    }

    fun getCatLiveData(): LiveData<MainState> {
        return catLiveData
    }

    fun getModelResponsesData(): LiveData<MainState> {
        return modelResponsesData
    }

    fun getWordHistory(): List<CurrentWord> {
        return wordHistory
    }

    fun setWordHistory(wordHistory: List<CurrentWord>) {
        this.wordHistory = wordHistory
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
