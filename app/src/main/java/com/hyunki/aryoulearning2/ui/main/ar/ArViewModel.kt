package com.hyunki.aryoulearning2.ui.main.ar

import android.app.Application
import android.net.Uri
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.ui.main.MainRepository
import com.google.ar.sceneform.rendering.ModelRenderable
import io.reactivex.Observable
import io.reactivex.Single

import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Function
import io.reactivex.internal.operators.flowable.FlowableBlockingSubscribe.subscribe
import io.reactivex.rxkotlin.subscribeBy
import io.reactivex.schedulers.Schedulers
import java.util.*

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepository: MainRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val modelLiveData = MutableLiveData<ArState>()

    private val futureModelMapListLiveData = MutableLiveData<ArState>()
    val futureLetterMap = MutableLiveData<HashMap<String, CompletableFuture<ModelRenderable>>>()

    val modelMapList = MutableLiveData<List<MutableMap<String, ModelRenderable>>>()
    val letterMap = MutableLiveData<HashMap<String, ModelRenderable>>()

    init {
        println("arviewmodel created")
    }

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = ArState.Success.OnModelsLoaded(models)
    }

    private fun onFutureModelMapListsFetched(futureModelMapList: List<MutableMap<String,CompletableFuture<ModelRenderable>>>) {
        futureModelMapListLiveData.value = ArState.Success.OnFutureModelMapListLoaded(futureModelMapList)
    }

    fun loadModels() {
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

    fun setListMapsOfFutureModels(modelList: List<Model>) {
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
                        onSuccess = { this.onFutureModelMapListsFetched(it) },
                        onError = {error ->
                            futureModelMapListLiveData.value = ArState.Error
                            onError(error)
                        }
                )
        compositeDisposable.add(listDisposable)
    }

    fun setMapOfFutureLetters(futureMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {

        val returnMap = HashMap<String, CompletableFuture<ModelRenderable>>()

        for (i in futureMapList.indices) {

            val modelName = futureMapList[i].keys.toString()
            for (j in modelName.indices) {
                returnMap[modelName[j].toString()] =
                        ModelRenderable.builder().setSource(
                                application, Uri.parse(modelName[j] + ".sfb")).build()
            }
        }
        futureLetterMap.value = returnMap
    }

    fun setLetterRenderables(futureLetterMap: HashMap<String, CompletableFuture<ModelRenderable>>) {
        val returnMap = HashMap<String, ModelRenderable>()

        for (e in futureLetterMap.entries) {

            CompletableFuture.allOf(e.value)
                    .handle<Unit> { notUsed, throwable ->
                        // When you build a Renderable, Sceneform loads its resources in the background while
                        // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                        // before calling get().
                        if (throwable != null) {
                            Log.e("completable future throwable", throwable.toString())
                        }
                        try {
                            returnMap[e.key] = e.value.get()
                        } catch (ex: InterruptedException) {
                        } catch (ex: ExecutionException) {
                        }
                    }
        }
        letterMap.value = returnMap
    }

    fun setModelRenderables(futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>) {
        val returnList = ArrayList<MutableMap<String, ModelRenderable>>()

        for (i in futureModelMapList.indices) {

            for (e in futureModelMapList[i].entries) {

                val modelMap = HashMap<String, ModelRenderable>()



                CompletableFuture.allOf(e.value)
                        .handle<Unit> { _, throwable ->
                            // When you build a Renderable, Sceneform loads its resources in the background while
                            // returning a CompletableFuture. Call handle(), thenAccept(), or check isDone()
                            // before calling get().

                            if (throwable != null) {
                                Log.e("completable future throwable", throwable.toString())
                            }

                            try {
                                modelMap[e.key] = e.value.get()
                            } catch (ex: InterruptedException) {
                            } catch (ex: ExecutionException) {
                            }
                        }
                returnList.add(modelMap)
            }
        }
        modelMapList.value = returnList
    }

    fun getModelLiveData(): MutableLiveData<ArState> {
        return modelLiveData
    }

    fun getFutureModelMapListLiveData(): MutableLiveData<ArState> {
        return futureModelMapListLiveData
    }

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message)
    }

    companion object {
        const val TAG = "ArViewModel"
    }
}
