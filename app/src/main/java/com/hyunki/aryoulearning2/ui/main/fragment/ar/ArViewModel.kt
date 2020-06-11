package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.app.Application
import android.net.Uri
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData

import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import io.reactivex.Observable
import io.reactivex.Single

import java.util.concurrent.CompletableFuture

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toMap
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepositoryImpl: MainRepository) : ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    private val modelLiveData = MutableLiveData<ArState>()
    private val futureModelMapListLiveData = MutableLiveData<ArState>()
    private val futureLetterMapLiveData = MutableLiveData<ArState>()
    private val modelMapListLiveData = MutableLiveData<ArState>()
    private val letterMapLiveData = MutableLiveData<ArState>()

    private var isModelsLoaded = false
    private var isLettersLoaded = false

//    private fun onModelsFetched(models: List<Model>) {
//        modelLiveData.value = ArState.Success.OnModelsLoaded(models)
//    }

    private fun onFutureModelMapListsLoaded(futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {
        futureModelMapListLiveData.value = ArState.Success.OnFutureModelMapListLoaded(futureModelMapList)
    }

    private fun onFutureLetterMapLoaded(futureLetterMap: MutableMap<String, CompletableFuture<ModelRenderable>>) {
        futureLetterMapLiveData.value = ArState.Success.OnFutureLetterMapLoaded(futureLetterMap)
    }

    private fun onLetterRenderableMapLoaded(letterMap: MutableMap<String, ModelRenderable>) {
        letterMapLiveData.value = ArState.Success.OnLetterMapLoaded(letterMap)
    }

    private fun onModelRenderableMapListLoaded(modelMapList: List<MutableMap<String, ModelRenderable>>) {
        modelMapListLiveData.value = ArState.Success.OnModelMapListLoaded(modelMapList)
    }

    fun getModelsFromRepositoryByCategory(category: String) = liveData(Dispatchers.IO) {
        emit(ArState.Loading)
        try {
            val result = mainRepositoryImpl.getModelsByCat(category)
            emit(ArState.Success.OnModelsLoaded(result))

        }catch (exception: Exception) {
            emit(ArState.Error)
        }
    }

//    fun fetchModelsFromRepository(category: String) {
//        modelLiveData.value = ArState.Loading
//        val modelDisposable = mainRepositoryImpl.getModelsByCat(category)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribeBy(
//                        onSuccess = { this.onModelsFetched(it) },
//                        onError = { error ->
//                            modelLiveData.value = ArState.Error
//                            onError(error)
//                        }
//                )
//        compositeDisposable.add(modelDisposable)
//    }

    fun loadListofMapsOfFutureModels(modelList: Single<List<Model>>) {
        futureModelMapListLiveData.value = ArState.Loading
        val listDisposable = modelList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flattenAsObservable { it }
                .flatMap {
                    val futureMap = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
                    futureMap[it.name] =
                            ModelRenderable.builder().setSource(
                                    application, Uri.parse(it.name + ".sfb")).build()
                    Observable.just(futureMap)
                }.toList()
                .subscribeBy(
                        onSuccess = { this.onFutureModelMapListsLoaded(it) },
                        onError = { error ->
                            futureModelMapListLiveData.value = ArState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(listDisposable)
    }

    fun loadMapOfFutureLetters(futureMapList: Observable<List<MutableMap<String, CompletableFuture<ModelRenderable>>>>) {
        futureLetterMapLiveData.value = ArState.Loading

        val futureModelMapDisposable = futureMapList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapIterable { it }
                .flatMap {
                    val wordArray = mutableListOf<Char>()
                    for (s in it.keys) {
                        for (c in s) {
                            wordArray.add(c)
                        }
                    }
                    Observable.just(wordArray)
                }
                .toList()
                .flattenAsObservable { it }
                .flatMapIterable { it }
                .flatMap {
                    val pair = Pair(it.toString(), ModelRenderable.builder().setSource(
                            application, Uri.parse("$it.sfb")).build())
                    Observable.just(pair)
                }
                .toMap()
                .subscribeBy(
                        onSuccess = { this.onFutureLetterMapLoaded(it) },
                        onError = { error ->
                            futureLetterMapLiveData.value = ArState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(futureModelMapDisposable)
    }

    fun loadLetterRenderables(futureLetterMap: Observable<MutableMap<String, CompletableFuture<ModelRenderable>>>) {
        val lettersCount = futureLetterMap.count()
        letterMapLiveData.value = ArState.Loading
        val futureLetterMapDisposable = futureLetterMap
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapSingle {
                    val m = mutableMapOf<String, ModelRenderable>()
                    for (e in it) {
                        CompletableFuture.allOf(e.value).thenAccept {
                            m[e.key] = e.value.get()
                            if (m.size == lettersCount.blockingGet().toInt()) {
                                isLettersLoaded = true
                            }
                        }
                    }
                    Single.just(m)
                }
                .subscribeBy(
                        onNext = { this.onLetterRenderableMapLoaded(it) },
                        onError = { error ->
                            letterMapLiveData.value = ArState.Error
                            onError(error)
                        })
        compositeDisposable.add(futureLetterMapDisposable)
    }

    fun loadModelRenderables(futureModelMapList: Observable<List<MutableMap<String, CompletableFuture<ModelRenderable>>>>) {
        val modelsCount = futureModelMapList.count()
        modelMapListLiveData.value = ArState.Loading
        val modelMapListDisposable = futureModelMapList
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap {
                    val list = mutableListOf<MutableMap<String, ModelRenderable>>()
                    for (m in it) {
                        for (e in m) {
                            val map = mutableMapOf<String, ModelRenderable>()
                            CompletableFuture.allOf(e.value).thenAccept {
                                map[e.key] = e.value.get()
                                list.add(map)
                                if (list.size == modelsCount.blockingGet().toInt()) {
                                    isModelsLoaded = true
                                }
                            }
                        }
                    }
                    Observable.just(list)
                }
                .subscribeBy(
                        onNext = { this.onModelRenderableMapListLoaded(it) },
                        onError = { error ->
                            modelMapListLiveData.value = ArState.Error
                            onError(error)
                        })
        compositeDisposable.add(modelMapListDisposable)
    }

    fun getModelLiveData(): MutableLiveData<ArState> {
        return modelLiveData
    }

    fun getFutureModelMapListLiveData(): MutableLiveData<ArState> {
        return futureModelMapListLiveData
    }

    fun getFutureLetterMapLiveData(): MutableLiveData<ArState> {
        return futureLetterMapLiveData
    }

    fun getModelMapListLiveData(): MutableLiveData<ArState> {
        return modelMapListLiveData
    }

    fun getLetterMapLiveData(): MutableLiveData<ArState> {
        return letterMapLiveData
    }

    fun isModelsLoaded(): Boolean {
        return isModelsLoaded
    }

    fun isLettersLoaded(): Boolean {
        return isLettersLoaded
    }

    private fun onError(throwable: Throwable) {
//        Log.d(TAG, throwable.message)
    }

    companion object {
        const val TAG = "arviewmodel"
    }
}
