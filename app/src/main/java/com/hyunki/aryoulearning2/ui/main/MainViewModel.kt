package com.hyunki.aryoulearning2.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
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
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject
internal constructor(private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val modelResponsesData = MutableLiveData<MainState>()
    private val modelLiveData = MutableLiveData<MainState>()
    private val catLiveData = MutableLiveData<MainState>()
    private var wordHistory: List<CurrentWord> = ArrayList()

    fun loadModelResponses() {
        modelResponsesData.value = MainState.Loading
        val modelResDisposable = mainRepositoryImpl.getModelResponses()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onNext = { onModelResponsesLoaded(it) },
                        onError = { error ->
                            modelResponsesData.value = MainState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(modelResDisposable)
    }

    private fun saveModelResponseData(modelResponses: ArrayList<ModelResponse>) {
        for (i in modelResponses.indices) {
            mainRepositoryImpl.insertCat(Category(
                    modelResponses[i].category,
                    modelResponses[i].background
            ))
            for (j in 0 until modelResponses[i].list.size) {
                mainRepositoryImpl.insertModel(Model(
                        modelResponses[i].category,
                        modelResponses[i].list[j].name,
                        modelResponses[i].list[j].image
                ))
            }
        }
    }

    fun loadModelsByCat(cat: String) {
        modelLiveData.value = MainState.Loading
        val modelDisposable = mainRepositoryImpl.getModelsByCat(cat)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { onModelsFetched(it) },
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
//        Log.d("MainViewModel", throwable.message)
    }

    private fun onModelsFetched(models: List<Model>) {
        modelLiveData.value = MainState.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        catLiveData.value = MainState.Success.OnCategoriesLoaded(categories)
    }

    private fun onModelResponsesLoaded(modelResponses: ArrayList<ModelResponse>) {
        saveModelResponseData(modelResponses)
        modelResponsesData.value = MainState.Success.OnModelResponsesLoaded(modelResponses)
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
