package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.app.Application
import android.net.Uri
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.MainRepository
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import io.reactivex.Observable
import io.reactivex.Single

import java.util.concurrent.CompletableFuture

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toMap
import io.reactivex.schedulers.Schedulers

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepository: MainRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val modelLiveData = MutableLiveData<ArState>()

    private val futureModelMapListLiveData = MutableLiveData<ArState>()
    private val futureLetterMapLiveData = MutableLiveData<ArState>()

    private val modelMapListLiveData = MutableLiveData<ArState>()
    private val letterMapLiveData = MutableLiveData<ArState>()

    private var isModelsLoaded = false
    private var isLettersLoaded = false

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = ArState.Success.OnModelsLoaded(models)
    }

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

    fun fetchModelsFromRepository(category: String) {
        modelLiveData.value = ArState.Loading
//        val catDisposable = mainRepository.currentCategory.subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe { currentCategory ->
                    val modelDisposable = mainRepository.getModelsByCat(category)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribeBy(
                                    onSuccess = { this.onModelsFetched(it) },
                                    onError = { error ->
                                        modelLiveData.value = ArState.Error
                                        onError(error)
                                    }
                            )
                    compositeDisposable.add(modelDisposable)
//                }
//        compositeDisposable.add(catDisposable)
    }

    fun loadListMapsOfFutureModels(modelList: List<Model>) {
        futureModelMapListLiveData.value = ArState.Loading

        val listDisposable = Single.just(modelList)
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

    fun loadMapOfFutureLetters(futureMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {

        futureModelMapListLiveData.value = ArState.Loading

        val futureModelMapDisposable = Single.just(futureMapList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flattenAsObservable { it }
                .flatMap { Observable.just(it.keys) }
                .flatMapIterable { it }
                .flatMap {
                    val wordArray = mutableListOf<Char>()
                    for (s in it) {
                        wordArray.add(s)
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
                            futureModelMapListLiveData.value = ArState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(futureModelMapDisposable)
    }

    fun loadLetterRenderables(futureLetterMap: MutableMap<String, CompletableFuture<ModelRenderable>>) {
        val lettersCount = futureLetterMap.size

        letterMapLiveData.value = ArState.Loading

        val futureLetterMapDisposable = Observable.just(futureLetterMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMapSingle {
                    val m = mutableMapOf<String, ModelRenderable>()
                    for (e in it) {
                        CompletableFuture.allOf(e.value).thenAccept {
                            m[e.key] = e.value.get()
                            if (m.size == lettersCount) {

                                isLettersLoaded = true
                            }
                            Log.d(TAG, m.size.toString())
                            Log.d(TAG, lettersCount.toString())
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

    fun loadModelRenderables(futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {
        val modelsCount = futureModelMapList.size
        modelMapListLiveData.value = ArState.Loading

        val modelMapListDisposable = Observable.just(futureModelMapList)
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
                                if (list.size == modelsCount) {
                                    isModelsLoaded = true
                                }
                                Log.d(TAG, m.size.toString())
                                Log.d(TAG, modelsCount.toString())
                            }
                        }
                    }

                    Observable.just(list)
                }
                .subscribeBy(
                        onNext = { this.onModelRenderableMapListLoaded(it) },
                        onError = { error ->
                            futureModelMapListLiveData.value = ArState.Error
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
        Log.d(TAG, throwable.message)
    }

    companion object {
        const val TAG = "arviewmodel"
    }
}
