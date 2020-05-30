package com.hyunki.aryoulearning2.ui.main.ar

import android.app.Application
import android.net.Uri
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.a.b.a.a.a.e

import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.ui.main.MainRepository
import com.google.ar.sceneform.rendering.ModelRenderable
import io.reactivex.Flowable
import io.reactivex.Observable
import io.reactivex.Single

import java.util.concurrent.CompletableFuture

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.internal.operators.observable.ObservableFromFuture
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.rxkotlin.toMap
import io.reactivex.schedulers.Schedulers
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.stream.Collectors.toList

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepository: MainRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val modelLiveData = MutableLiveData<ArState>()

    private val futureModelMapListLiveData = MutableLiveData<ArState>()
    private val futureLetterMapLiveData = MutableLiveData<ArState>()

    private val modelMapListLiveData = MutableLiveData<ArState>()
    private val letterMapLiveData = MutableLiveData<ArState>()

    init {
        println("arviewmodel created")
    }

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

    fun fetchModelsFromRepository() {
        modelLiveData.value = ArState.Loading
        val catDisposable = mainRepository.currentCategory.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { currentCategory ->
                    val modelDisposable = mainRepository.getModelsByCat(currentCategory.currentCategory)
                            .subscribeBy(
                                    onSuccess = { this.onModelsFetched(it) },
                                    onError = { error ->
                                        modelLiveData.value = ArState.Error
                                        onError(error)
                                    }
                            )
                    compositeDisposable.add(modelDisposable)
                }
        compositeDisposable.add(catDisposable)
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
                .flattenAsObservable { it -> it }
                .flatMap { Observable.just(it.keys) }
                .flatMapIterable { it -> it }
                .flatMap {
                    val wordArray = mutableListOf<Char>()
                    for (s in it) {
                        wordArray.add(s)
                    }
                    Observable.just(wordArray)
                }
                .toList()
                .flattenAsFlowable { it }
                .flatMapIterable { it }
                .flatMap {

                    val pair = Pair(it.toString(), ModelRenderable.builder().setSource(
                            application, Uri.parse("$it.sfb")).build())

                    Flowable.just(pair)
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

        letterMapLiveData.value = ArState.Loading

        val futureLetterMapDisposable = Observable.just(futureLetterMap)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap { Observable.just(it.entries) }
                .flatMapIterable { it -> it }
                .flatMap { Observable.just(it) }
                .flatMap{

                    val m = mutableMapOf<String, ModelRenderable>()

                    CompletableFuture.allOf(it.value)
                            .handle<Unit> { notUsed, throwable ->
                                // When you build a Renderable, Sceneform loads its resources in the background while
                                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                                // before calling get().
                                if (throwable != null) {
                                    return@handle null
                                }
                                try {
                                    m[it.key] = it.value.get()
                                } catch (ex: InterruptedException) {
                                } catch (ex: ExecutionException) {
                                }
                            }

                    Log.d(TAG, it.key.toString())
                    Log.d(TAG, it.value.toString())
                    Log.d(TAG, m.values.toString())
                    Log.d(TAG, m.keys.toString())

                    Observable.just(m)
                }

                .flatMapIterable { it.entries }
                .flatMap {
                    val pair = Pair(it.key, it.value)
                    Observable.just(pair)
                }
                .toMap()
                .subscribeBy(
                        onSuccess = { this.onLetterRenderableMapLoaded(it) },
                        onError = { error ->
                            letterMapLiveData.value = ArState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(futureLetterMapDisposable)
    }

    fun loadModelRenderables(futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {

        modelMapListLiveData.value = ArState.Loading

        val modelMapListDisposable = Single.just(futureModelMapList)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flattenAsObservable { it -> it }
                .flatMap { Observable.just(it.entries) }
                .flatMapIterable { it -> it }
                .flatMap{
                    val m = mutableMapOf<String, ModelRenderable>()
                    CompletableFuture.allOf(it.value)
                            .handle<Unit> { notUsed, throwable ->
                                // When you build a Renderable, Sceneform loads its resources in the background while
                                // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                                // before calling get().
                                if (throwable != null) {
                                    Log.d(TAG, throwable.message)
                                    return@handle null
                                }
                                try {
                                    m[it.key] = it.value.get()
                                    Log.d(TAG, "condition reached: try get() future")
                                } catch (ex: InterruptedException) {
                                } catch (ex: ExecutionException) {
                                }
                            }
                    Observable.just(m)
                }
                .toList()
                                    .subscribeBy(
                                    onSuccess = { this.onModelRenderableMapListLoaded(it) },
                                    onError = { error ->
                                        futureModelMapListLiveData.value = ArState.Error
                                        onError(error)
                                    })

//    compositeDisposable.add(modelMapListDisposable)
    }

//    private fun getModelRenderable(completable: CompletableFuture<ModelRenderable>): ModelRenderable {
//        lateinit var renderable: ModelRenderable
//
//        if (!completable.isDone) {
//            CompletableFuture.allOf(completable)
//                    .thenAccept {
//                        renderable = completable.get()
//                    }
//                    .exceptionally { it ->
//                        Log.e(TAG, it.message)
//                        return@exceptionally null
//                    }
//        }
//
//        return renderable
//    }

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

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message)
    }

    companion object {
        const val TAG = "ArViewModel"
    }
}
