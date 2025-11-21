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
import io.reactivex.Completable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*
import javax.inject.Inject

class MainViewModel @Inject
internal constructor(private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()
    private val _modelResponsesData = MutableLiveData<MainState>()
    val modelResponsesData: LiveData<MainState> get() = _modelResponsesData
    private val _modelLiveData = MutableLiveData<MainState>()
    val modelLiveData: LiveData<MainState> get() = _modelLiveData
    private val _catLiveData = MutableLiveData<MainState>()
    val catLiveData: LiveData<MainState> get() = _catLiveData
    private var _currentCategory: MutableLiveData<String?> = MutableLiveData(null)
    val currentCategory: String? get() = _currentCategory.value
    fun setCurrentCategory(cat: String) {
        _currentCategory.value = cat
    }

    fun loadModelResponses() {
        _modelResponsesData.value = MainState.Loading
        val modelResDisposable = mainRepositoryImpl.getModelResponses()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onNext = {
                    onModelResponsesLoaded(it)
                },
                onError = { error ->
                    _modelResponsesData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(modelResDisposable)
    }

    private fun saveModelResponseDataCategories(categories: ArrayList<Category>) {
        for (i in categories.indices) {
            mainRepositoryImpl.insertCat(categories[i])
        }
    }

    private fun saveModelResponseDataModels(models: ArrayList<Model>) {
        models.forEach {
            mainRepositoryImpl.insertModel(it)
        }
    }

    fun loadModelsByCat() {
        val cat = this.currentCategory ?: return
        _modelLiveData.value = MainState.Loading
        val modelDisposable = mainRepositoryImpl.getModelsByCat(cat)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = {
                    onModelsFetched(it)
                },
                onError = { error ->
                    _modelLiveData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(modelDisposable)
    }

    fun loadCategories() {
        _catLiveData.value = MainState.Loading
        val catDisposable = mainRepositoryImpl.getAllCats()
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribeBy(
                onSuccess = { this.onCatsFetched(it) },
                onError = { error ->
                    _catLiveData.value = MainState.Error
                    onError(error)
                }
            )
        compositeDisposable.add(catDisposable)
    }

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message.toString())
    }

    private fun onModelsFetched(models: List<Model>) {
        _modelLiveData.value = MainState.Success.OnModelsLoaded(models)
    }

    private fun onCatsFetched(categories: List<Category>) {
        _catLiveData.value = MainState.Success.OnCategoriesLoaded(categories)
    }

    private fun onModelResponsesLoaded(modelResponses: ArrayList<ModelResponse>) {
        _modelResponsesData.value = MainState.Success.OnModelResponsesLoaded(modelResponses)
        val categories = getCategoriesToSaveFromModelResponseData(modelResponses)
        val models = getModelsToSaveFromModelResponseData(modelResponses)

        Completable.fromAction {
            saveModelResponseDataCategories(categories)
            saveModelResponseDataModels(models)
        }
            .subscribeOn(Schedulers.io())
            .subscribe({}, { onError(it) })
            .let(compositeDisposable::add)
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

    private fun getModelsToSaveFromModelResponseData(modelResponses: List<ModelResponse>): ArrayList<Model> {
        val models = arrayListOf<Model>()

        modelResponses.forEach { response ->
            response.list.forEach { item ->
                item.category = response.category
                models.add(item)
            }
        }
        return models
    }

    // TODO: remove, for database development
    fun clearEntireDatabase() {
        mainRepositoryImpl.clearEntireDatabase()
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
        //        clearEntireDatabase();
    }
}
