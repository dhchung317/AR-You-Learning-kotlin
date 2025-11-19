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

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepositoryImpl: MainRepository) :
    ViewModel() {
    private val compositeDisposable = CompositeDisposable()

    //    private val modelLiveData = MutableLiveData<ArState>()
    private val futureModelMapListLiveData = MutableLiveData<ArState>()
    private val futureLetterMapLiveData = MutableLiveData<ArState>()
    private val modelMapListLiveData = MutableLiveData<ArState>()
    private val letterMapLiveData = MutableLiveData<ArState>()

    private var isModelsLoaded = false
    private var isLettersLoaded = false

    fun loadListofMapsOfFutureModels(models: List<Model>) {
        futureModelMapListLiveData.value = ArState.Loading

        // Build futures synchronously
        val futureList = models.map { model ->
            mutableMapOf(
                model.name to ModelRenderable.builder()
                    .setSource(application, Uri.parse(model.name + ".sfb"))
                    .build()
            )
        }

        futureModelMapListLiveData.value =
            ArState.Success.OnFutureModelMapListLoaded(futureList)
    }

    fun loadLetterFuturesFromModels(models: List<Model>) {
        futureLetterMapLiveData.value = ArState.Loading

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

        futureLetterMapLiveData.value =
            ArState.Success.OnFutureLetterMapLoaded(futureMap)
    }

    fun loadLetterRenderables(
        futureLetterMap: Map<String, CompletableFuture<ModelRenderable>>
    ) {
        letterMapLiveData.value = ArState.Loading

        val result = mutableMapOf<String, ModelRenderable>()
        val allFutures = mutableListOf<CompletableFuture<*>>()

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
                letterMapLiveData.postValue(
                    ArState.Success.OnLetterMapLoaded(result)
                )
            }
    }

    fun loadModelRenderables(
        futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>
    ) {
        modelMapListLiveData.value = ArState.Loading

        val resultList = mutableListOf<MutableMap<String, ModelRenderable>>()
        val allFutures = mutableListOf<CompletableFuture<*>>()

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
                modelMapListLiveData.postValue(
                    ArState.Success.OnModelMapListLoaded(resultList)
                )
            }
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
    }
}
