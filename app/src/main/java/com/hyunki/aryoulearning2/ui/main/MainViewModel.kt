package com.hyunki.aryoulearning2.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject
internal constructor(private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val modelResponsesData = MutableLiveData<MainState>()
    private val modelLiveData = MutableLiveData<MainState>()
    private val catLiveData = MutableLiveData<MainState>()
    private var wordHistory: List<CurrentWord> = ArrayList()

    private var _currentCategory: MutableLiveData<String?> = MutableLiveData(null)
    val currentCategory: String? get() = _currentCategory.value
    fun setCurrentCategory(cat: String) {
        _currentCategory.value = cat
    }

    fun loadModelResponses() {
        modelResponsesData.value = MainState.Loading
        val modelResDisposable = mainRepositoryImpl.getModelResponses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    onModelResponsesLoaded(it)
                },
                onError = { error ->
                    modelResponsesData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(modelResDisposable)
    }

    private fun saveModelResponseDataCategories(categories: ArrayList<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
        mainRepositoryImpl.getModelsByCat("Animals")
    }

    private fun saveModelResponseDataModels(models: ArrayList<Model>) {
        models.forEach {
            mainRepositoryImpl.insertModel(it)
        }
    }

    fun loadModelsByCat() {
        val cat = this.currentCategory ?: return
        modelLiveData.value = MainState.Loading
        val modelDisposable = mainRepositoryImpl.getModelsByCat(cat)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    onModelsFetched(it)
                },
                onError = { error ->
                    modelLiveData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(modelDisposable)
    }

    fun loadCategories() {
        catLiveData.value = MainState.Loading
        val catDisposable = mainRepositoryImpl.getAllCats()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { this.onCatsFetched(it) },
                onError = { error ->
                    catLiveData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(catDisposable)
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

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message.toString())
    }

    private fun onModelsFetched(models: List<Model>) {
        modelLiveData.value = MainState.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        catLiveData.value = MainState.Success.OnCategoriesLoaded(categories)
    }

    private fun onModelResponsesLoaded(modelResponses: ArrayList<ModelResponse>) {
        modelResponsesData.value = MainState.Success.OnModelResponsesLoaded(modelResponses)
        saveModelResponseDataCategories(getCategoriesToSaveFromModelResponseData(modelResponses))

        saveModelResponseDataModels(getModelsToSaveFromModelResponseData(modelResponses))
    }

    private fun getCategoriesToSaveFromModelResponseData(modelResponses: ArrayList<ModelResponse>): ArrayList<Category> {
        val categories = arrayListOf<Category>()
        for (i in modelResponses.indices) {
            categories.add(
                Category(
                    modelResponses[i].category,
                    modelResponses[i].background
                )
            )
        }
        return categories
    }

    private fun getModelsToSaveFromModelResponseData(modelResponses: ArrayList<ModelResponse>): ArrayList<Model> {
        val models = arrayListOf<Model>()

        modelResponses.forEach {
            it.list.map { item ->
                item.category = it.category
                models.add(item)
            }

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
}
