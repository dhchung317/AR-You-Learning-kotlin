package com.hyunki.aryoulearning2.ui.main

import android.util.Log

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.CurrentCategory
import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.ar.util.CurrentWord

import java.util.ArrayList

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class MainViewModel @Inject
internal constructor(private val mainRepository: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    val modelResponsesData = MutableLiveData<State>()
    val modelLiveData = MutableLiveData<State>()
    val catLiveData = MutableLiveData<State>()
    val curCatLiveData = MutableLiveData<State>()
    var wordHistory: List<CurrentWord> = ArrayList()

    fun loadModelResponses() {
        modelResponsesData.value = State.Loading
        compositeDisposable.add(
                mainRepository.modelResponses
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe({ modelResponses ->
                            if (modelResponses.size > 0) {
                                saveModelResponseData(modelResponses)
                                modelResponsesData.value = State.Success.OnModelResponsesLoaded(modelResponses)
                            }

                        }, { throwable -> modelResponsesData.setValue(State.Error) })
        )
    }

    fun saveModelResponseData(modelResponses: ArrayList<ModelResponse>) {
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
        modelLiveData.value = State.Loading
        Log.d(TAG, "loadModelsByCat: loading models by cat")
        val modelDisposable = mainRepository.getModelsByCat(cat)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onModelsFetched(it) }, { this.onError(it) })
        compositeDisposable.add(modelDisposable)
    }

    fun loadCategories() {
        catLiveData.value = State.Loading
        val catDisposable = mainRepository.allCats
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onCatsFetched(it) }, { this.onError(it) })
        compositeDisposable.add(catDisposable)
    }

    fun loadCurrentCategoryName() {
        curCatLiveData.value = State.Loading
        val curCatDisposable = mainRepository.currentCategory
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ this.onCurCatsFetched(it) }, { this.onError(it) })
        compositeDisposable.add(curCatDisposable)
    }

    fun getModelLiveData(): LiveData<State> {
        return modelLiveData
    }

    fun getCatLiveData(): LiveData<State> {
        return catLiveData
    }

    fun getCurCatLiveData(): LiveData<State> {
        return curCatLiveData
    }

    internal fun getModelResponsesData(): LiveData<State> {
        return modelResponsesData
    }

    fun setCurrentCategory(category: Category) {
        mainRepository.setCurrentCategory(CurrentCategory(category.name))
    }

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message)
    }

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = State.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        Log.d(TAG, "onCatsFetched: " + categories.size)
        catLiveData.value = State.Success.OnCategoriesLoaded(categories)
    }

    private fun onCurCatsFetched(category: CurrentCategory) {
        Log.d(TAG, "onCurCatsFetched: " + category.currentCategory)
        curCatLiveData.value = State.Success.OnCurrentCategoryStringLoaded(category.currentCategory)
        Log.d(TAG, "onCurCatsFetched: " + State.Success.OnCurrentCategoryStringLoaded(category.currentCategory).javaClass)
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
