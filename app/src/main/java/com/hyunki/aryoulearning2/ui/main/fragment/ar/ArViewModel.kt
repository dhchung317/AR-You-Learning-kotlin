package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.app.Application
import android.net.Uri
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.DispatcherProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withContext
import java.util.concurrent.CompletableFuture
import javax.inject.Inject

class ArViewModel
@Inject
constructor(
        private val application: Application,
        private val mainRepositoryImpl: MainRepository,
        private val defaultDispatcher: DispatcherProvider) : ViewModel() {

    private var isModelsLoaded = false
    private var isLettersLoaded = false

    fun getModelsFromRepositoryByCategory(category: String) = liveData(Dispatchers.IO) {
        emit(ArState.Loading)
        try {
            val result = mainRepositoryImpl.getModelsByCat(category)
            emit(ArState.Success.OnModelsLoaded(result))
        } catch (exception: Exception) {
            emit(ArState.Error(exception.localizedMessage))
        }
    }

    //TODO fix use of await
    @ExperimentalCoroutinesApi
    fun getListOfMapsOfFutureModels(modelList: List<Model>) = liveData(defaultDispatcher.io()) {
        emit(ArState.Loading)
        try {
            withContext(defaultDispatcher.main()) {
                val list = async {
                    modelList.asFlow()
                            .map {
                                mapOf(
                                        Pair(it.name,
                                        ModelRenderable.builder().setSource(application, Uri.parse(it.name + ".sfb")).build())
                                )
                            }.toList()
                }.await()

                emit(ArState.Success.OnFutureModelMapListLoaded(list))
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.localizedMessage))
        }
    }

    //TODO refactor long chains
    @ExperimentalCoroutinesApi
    fun getMapOfFutureLetters(futureModelMapList: List<Map<String, CompletableFuture<ModelRenderable>>>) = liveData(defaultDispatcher.io()) {
        emit(ArState.Loading)

        try {
            withContext(defaultDispatcher.main()) {
                val wordList = async {
                    futureModelMapList.asFlow()
                            .map { it.keys }
                            .map { it.first().toList() }
                            .toList()
                }.await()

                val charList = async {
                    wordList.flatten().asFlow()
                            .map { c ->
                                Pair(c.toString(),
                                        ModelRenderable.builder().setSource(application, Uri.parse("${c}.sfb")).build())
                            }.toList()
                }.await()
                val map = charList.associateBy({ it.first }, { it.second })

                emit(ArState.Success.OnFutureLetterMapLoaded(map))
            }

        } catch (exception: Exception) {
            emit(ArState.Error(exception.localizedMessage))
        }
    }

    fun getLetterRenderables(futureLetterMap: Map<String, CompletableFuture<ModelRenderable>>) = liveData(defaultDispatcher.io()) {
        val count = futureLetterMap.size
        emit(ArState.Loading)
        try {
            withContext(defaultDispatcher.default()) {
                val list = async {
                    futureLetterMap.entries.asFlow()
                            .map { it ->
                                Pair(it.key, it.value.join())
                            }
                            .toList()
                }
                val map = list.await().associateBy({ it.first }, { it.second })
                emit(ArState.Success.OnLetterMapLoaded(map))
                if (map.size == count) {
                    isLettersLoaded = true
                }
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.localizedMessage))
        }
    }

    fun getModelRenderables(futureModelMapList: List<Map<String, CompletableFuture<ModelRenderable>>>) = liveData(defaultDispatcher.io()) {
        val count = futureModelMapList.size
        emit(ArState.Loading)
        try {
            withContext(defaultDispatcher.default()) {
                val list = async {
                    futureModelMapList.asSequence().asIterable()
                            .map { it.entries }
                            .flatten()
                            .map { mutableMapOf(Pair(it.key, it.value.join())) }
                            .toList()
                }.await()


                if (list.size == count) {
                    Log.d(TAG, "getModelRenderables: " + list.size)
                    isModelsLoaded = true
                }

                emit(ArState.Success.OnModelMapListLoaded(list))
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.localizedMessage))
        }
    }

    fun isModelsLoaded(): Boolean {
        return isModelsLoaded
    }

    fun isLettersLoaded(): Boolean {
        return isLettersLoaded
    }

    companion object {
        const val TAG = "arviewmodel"
    }
}
