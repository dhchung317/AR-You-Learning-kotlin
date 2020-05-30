package com.hyunki.aryoulearning2.ui.main

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.CurrentCategory
import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.ar.util.CurrentWord
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject
internal constructor(private val mainRepository: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val modelResponsesData = MutableLiveData<MainState>()
    private val modelLiveData = MutableLiveData<MainState>()
    private val catLiveData = MutableLiveData<MainState>()
    private val curCatLiveData = MutableLiveData<MainState>()
    private var wordHistory: List<CurrentWord> = ArrayList()

    fun loadModelResponses() {
        modelResponsesData.value = MainState.Loading
        val modelResDisposable = mainRepository.modelResponses
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
            mainRepository.insertCat(Category(
                    modelResponses[i].category,
                    modelResponses[i].background
            ))
            for (j in 0 until modelResponses[i].list.size) {
                Log.d(TAG, "observeModelResponses: " + modelResponses[i].list[j].name)
                mainRepository.insertModel(Model(
                        modelResponses[i].category,
                        modelResponses[i].list[j].name,
                        modelResponses[i].list[j].image
                ))
            }
        }
    }

    fun loadModelsByCat(cat: String) {
        modelLiveData.value = MainState.Loading
        Log.d(TAG, "loadModelsByCat: loading models by cat")
        val modelDisposable = mainRepository.getModelsByCat(cat)
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
        val catDisposable = mainRepository.allCats
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

    fun loadCurrentCategoryName() {
        curCatLiveData.value = MainState.Loading
        val curCatDisposable = mainRepository.currentCategory
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeBy(
                        onSuccess = { this.onCurCatsFetched(it) },
                        onError = { error ->
                            curCatLiveData.value = MainState.Error
                            onError(error)
                        })
        compositeDisposable.add(curCatDisposable)
    }

    fun getModelLiveData(): LiveData<MainState> {
        return modelLiveData
    }

    fun getCatLiveData(): LiveData<MainState> {
        return catLiveData
    }

    fun getCurCatLiveData(): LiveData<MainState> {
        return curCatLiveData
    }

    fun getModelResponsesData(): LiveData<MainState> {
        return modelResponsesData
    }

    fun setCurrentCategory(category: Category) {
        mainRepository.setCurrentCategory(CurrentCategory(category.name))
    }

    fun getWordHistory(): List<CurrentWord> {
        return wordHistory
    }

    fun setWordHistory(wordHistory: List<CurrentWord>) {
        this.wordHistory = wordHistory
    }

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message)
    }

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = MainState.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        Log.d(TAG, "onCatsFetched: " + categories.size)
        catLiveData.value = MainState.Success.OnCategoriesLoaded(categories)
    }

    private fun onCurCatsFetched(category: CurrentCategory) {
        Log.d(TAG, "onCurCatsFetched: " + category.currentCategory)
        curCatLiveData.value = MainState.Success.OnCurrentCategoryStringLoaded(category.currentCategory)
        Log.d(TAG, "onCurCatsFetched: " + MainState.Success.OnCurrentCategoryStringLoaded(category.currentCategory).javaClass)
    }

    private fun onModelResponsesLoaded(modelResponses: ArrayList<ModelResponse>) {
        saveModelResponseData(modelResponses)
        modelResponsesData.value = MainState.Success.OnModelResponsesLoaded(modelResponses)
    }

    fun clearEntireDatabase() {
        mainRepository.clearEntireDatabase()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        //        clearEntireDatabase();
    }

    companion object {
        const val TAG = "MainViewModel"
    }
}
