package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.app.Application
import android.net.Uri
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.hyunki.aryoulearning2.data.db.model.Model
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import java.util.concurrent.CompletableFuture
import javax.inject.Inject
import io.reactivex.disposables.CompositeDisposable
import androidx.core.net.toUri
import androidx.lifecycle.LiveData

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepositoryImpl: MainRepository) :
    ViewModel() {
    private val _futureModelMapListLiveData = MutableLiveData<ArState>()
    val futureModelMapListLiveData: LiveData<ArState> get() = _futureModelMapListLiveData
    private val _futureLetterMapLiveData = MutableLiveData<ArState>()
    val futureLetterMapLiveData: LiveData<ArState> get() = _futureLetterMapLiveData
    private val _modelMapListLiveData = MutableLiveData<ArState>()
    val modelMapListLiveData: LiveData<ArState> get() = _modelMapListLiveData
    private val _letterMapLiveData = MutableLiveData<ArState>()
    val letterMapLiveData: LiveData<ArState> get() = _letterMapLiveData

    private var isModelsLoaded = false
    private var isLettersLoaded = false

    fun loadListofMapsOfFutureModels(models: List<Model>) {
        _futureModelMapListLiveData.value = ArState.Loading
        if (models.isEmpty()) _futureModelMapListLiveData.value = ArState.Error

        // Build futures synchronously
        val futureList = models.map { model ->
            mutableMapOf(
                model.name to ModelRenderable.builder()
                    .setSource(application, (model.name + ".sfb").toUri())
                    .build()
            )
        }

        _futureModelMapListLiveData.value =
            ArState.Success.OnFutureModelMapListLoaded(futureList)
    }

    fun loadLetterFuturesFromModels(models: List<Model>) {
        _futureLetterMapLiveData.value = ArState.Loading

        // Collect all distinct letters from model names
        val letters = mutableSetOf<Char>()
        models.forEach { model ->
            model.name.forEach { c ->
                letters.add(c)
            }
        }

        val futureMap = mutableMapOf<String, CompletableFuture<ModelRenderable>>()
        letters.forEach { c ->
            val key = c.toString()
            futureMap[key] = ModelRenderable.builder()
                .setSource(application, "$key.sfb".toUri())
                .build()
        }

        _futureLetterMapLiveData.value =
            ArState.Success.OnFutureLetterMapLoaded(futureMap)
    }

    fun loadLetterRenderables(
        futureLetterMap: Map<String, CompletableFuture<ModelRenderable>>
    ) {
        _letterMapLiveData.value = ArState.Loading

        val result = mutableMapOf<String, ModelRenderable>()
        val allFutures = mutableListOf<CompletableFuture<*>>()
        try {

            futureLetterMap.forEach { (key, future) ->
                val cf = future.thenAccept { renderable ->
                    synchronized(result) {
                        result[key] = renderable
                    }
                }
                allFutures.add(cf)
            }

            CompletableFuture.allOf(*allFutures.toTypedArray())
                .thenAccept {
                    isLettersLoaded = true
                    _letterMapLiveData.postValue(
                        ArState.Success.OnLetterMapLoaded(result)
                    )
                }.exceptionally { _ ->
                    _letterMapLiveData.postValue(ArState.Error)
                    null
                }
        } catch (_: Error) {
            _letterMapLiveData.value = ArState.Error
        }
    }

    fun loadModelRenderables(
        futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>
    ) {
        _modelMapListLiveData.value = ArState.Loading

        val resultList = mutableListOf<MutableMap<String, ModelRenderable>>()
        val allFutures = mutableListOf<CompletableFuture<*>>()
        try {
            // For each entry, attach a continuation to build the final map list
            futureModelMapList.forEach { futureMap ->
                futureMap.forEach { (key, future) ->
                    val cf = future.thenAccept { renderable ->
                        val singleMap = mutableMapOf<String, ModelRenderable>()
                        singleMap[key] = renderable
                        synchronized(resultList) {
                            resultList.add(singleMap)
                        }
                    }
                    allFutures.add(cf)
                }
            }

            // Wait for all to complete, then post the result
            CompletableFuture.allOf(*allFutures.toTypedArray())
                .thenAccept {
                    isModelsLoaded = true
                    _modelMapListLiveData.postValue(
                        ArState.Success.OnModelMapListLoaded(resultList)
                    )
                }

        } catch (_: Error) {
            _modelMapListLiveData.postValue(ArState.Error)
        }
    }

    fun isModelsLoaded(): Boolean {
        return isModelsLoaded
    }

    fun isLettersLoaded(): Boolean {
        return isLettersLoaded
    }

    private fun onError(throwable: Throwable) {
    }
}
