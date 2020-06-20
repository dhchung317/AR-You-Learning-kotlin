package com.hyunki.aryoulearning2.ui.main.fragment.ar

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.util.DispatcherProvider
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
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

    fun getModelsFromRepositoryByCategory(category: String) = liveData(defaultDispatcher.io()) {
        emit(ArState.Loading)
        try {
            val result = mainRepositoryImpl.getModelsByCat(category)
            emit(ArState.Success.OnModelsLoaded(result))
        } catch (exception: Exception) {
            emit(ArState.Error(exception.message.toString()))
        }
    }

    @ExperimentalCoroutinesApi
    fun getListOfMapsOfFutureModels(modelList: List<Model>) = liveData(defaultDispatcher.io()) {
        emit(ArState.Loading)
        try {
            withContext(defaultDispatcher.main()) {
                val list = async {
                    modelList
                            .map {
                                mapOf(
                                        Pair(it.name,
                                        ModelRenderable.builder().setSource(application, Uri.parse(it.name + ".sfb")).build())
                                )
                            }.toList()
                }

                emit(ArState.Success.OnFutureModelMapListLoaded(list.await()))
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.message.toString()))
        }
    }

    //TODO refactor long chains
    @ExperimentalCoroutinesApi
    fun getMapOfFutureLetters(futureModelMapList: List<Map<String, CompletableFuture<ModelRenderable>>>) = liveData(defaultDispatcher.io()) {
        emit(ArState.Loading)

        try {
            withContext(defaultDispatcher.main()) {
                val wordList = async {
                    futureModelMapList
                            .map { it.keys }
                            .map { it.first().toList() }
                            .toList()

                }

                val charList = async {
                    wordList.await().flatten()
                            .map { c ->
                                Pair(c.toString(),
                                        ModelRenderable.builder().setSource(application, Uri.parse("${c}.sfb")).build())
                            }
                            .toList()
                }
                val map = charList.await().associateBy({ it.first }, { it.second })

                emit(ArState.Success.OnFutureLetterMapLoaded(map))
            }

        } catch (exception: Exception) {
            emit(ArState.Error(exception.message.toString()))
        }
    }

    fun getLetterRenderables(futureLetterMap: Map<String, CompletableFuture<ModelRenderable>>) = liveData(defaultDispatcher.io()) {

        emit(ArState.Loading)

        val count = futureLetterMap.size
        try {
            withContext(defaultDispatcher.default()) {
                val list = async {
                    futureLetterMap.entries
                            .map { it ->
                                Pair(it.key, it.value.join())
                            }
                            .toList()
                }
                val map = list.await().associateBy({ it.first }, { it.second })
                if (count > 0 && map.size == count) {
                    isLettersLoaded = true
                    emit(ArState.Success.OnLetterMapLoaded(map))
                }else{
                    emit(ArState.Error("map returned empty, check input"))
                }
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.message.toString()))
        }
    }

    fun getModelRenderables(futureModelMapList: List<Map<String, CompletableFuture<ModelRenderable>>>) = liveData(defaultDispatcher.io()) {
        val count = futureModelMapList.size
        emit(ArState.Loading)
        try {
            withContext(defaultDispatcher.default()) {
                val list = async {
                    futureModelMapList
                            .map { it.entries }.flatten()
                            .map { mutableMapOf(Pair(it.key, it.value.join())) }.toList()
                }.await()

                if (count > 0 && list.size == count) {
                    isModelsLoaded = true
                    emit(ArState.Success.OnModelMapListLoaded(list))
                } else {
                    emit(ArState.Error("list returned empty, check input"))
                }
            }
        } catch (exception: Exception) {
            emit(ArState.Error(exception.message.toString()))
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
