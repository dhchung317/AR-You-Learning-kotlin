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

class MainViewModel @Inject
internal constructor(private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

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
//        val cats = async { parseCategoriesToSaveFromModelResponseData(data) }
        return parseModelsToSaveFromModelResponseData(data)
//        Log.d(TAG, "saveModelResponseData: " + models.await().size)

    }

    private fun getCategories(data: List<ModelResponse>): List<Category> {
        return parseCategoriesToSaveFromModelResponseData(data)
    }


//    fun loadModelResponses() {
//        modelResponsesData.value = MainState.Loading
//        val modelResDisposable = mainRepositoryImpl.getModelResponses()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy(
//                        onNext = { onModelResponsesLoaded(it) },
//                        onError = { error ->
//                            modelResponsesData.value = MainState.Error
//                            onError(error)
//                        }
//                )
//        compositeDisposable.add(modelResDisposable)
//    }


    private suspend fun saveCategories(categories: List<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
    }

    private suspend fun saveModels(models: List<Model>): List<Long> {

        return mainRepositoryImpl.insertAllModels(*models.toTypedArray())

//        for (i in models.indices) {
//            val x = mainRepositoryImpl.insertModel(models[i])
//            Log.d(TAG, "saveModels: " + x)
//        }

    }

//    fun getModelsByCat(cat: String): LiveData<List<Model>> {
//        return mainRepositoryImpl.getModelsByCat(cat)
//    }

//        Log.d(TAG, "loadModelsByCat: " + cat)
//
//        modelLiveData.value = MainState.Loading
//
//        val modelDisposable = mainRepositoryImpl.getModelsByCat(cat)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy(
//                        onSuccess = { onModelsFetched(it) },
//                        onError = { error ->
//                            modelLiveData.value = MainState.Error(error.localizedMessage)
//                            Log.d(TAG, "loadModelsByCat: " + error.localizedMessage)
////                            onError(error)
//                        }
//                )
//        compositeDisposable.add(modelDisposable)
//    }

//    fun loadCategories() {
//        catLiveData.value = MainState.Loading
//        val catDisposable = mainRepositoryImpl.getAllCats()
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy(
//                        onSuccess = { this.onCatsFetched(it) },
//                        onError = { error ->
//                            catLiveData.value = MainState.Error(error.localizedMessage)
////                            onError(error)
//                        }
//                )
//        compositeDisposable.add(catDisposable)
//    }

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

//    private fun onError(throwable: Throwable) {
////        Log.d("MainViewModel", throwable.message)
//    }

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = MainState.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        catLiveData.value = MainState.Success.OnCategoriesLoaded(categories)
    }

    private fun onModelResponsesLoaded() {
//        modelResponsesData.value = MainState.Success.OnModelResponsesLoaded(modelResponses)
//        saveModelResponseDataCategories(getCategoriesToSaveFromModelResponseData(modelResponses))
//        saveModelResponseDataModels(getModelsToSaveFromModelResponseData(modelResponses))
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
        compositeDisposable.clear()
        //        clearEntireDatabase();
    }

    companion object {
        const val TAG = "mainviewmodel"
    }
}
