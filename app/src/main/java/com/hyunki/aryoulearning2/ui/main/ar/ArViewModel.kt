package com.hyunki.aryoulearning2.ui.main.ar

import android.app.Application
import android.net.Uri
import android.util.Log

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.ui.main.MainRepository
import com.google.ar.sceneform.rendering.ModelRenderable

import java.util.ArrayList
import java.util.HashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException

import javax.inject.Inject

import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers

class ArViewModel @Inject
constructor(private val application: Application, private val mainRepository: MainRepository) : ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    val modelLiveData = MutableLiveData<List<Model>>()

    val futureModelMapList = MutableLiveData<List<MutableMap<String, CompletableFuture<ModelRenderable>>>>()
    val futureLetterMap = MutableLiveData<HashMap<String, CompletableFuture<ModelRenderable>>>()

    val modelMapList = MutableLiveData<List<MutableMap<String, ModelRenderable>>>()
    val letterMap = MutableLiveData<HashMap<String, ModelRenderable>>()

    init {
        println("arviewmodel created")
    }

    private fun onModelsFetched(models: List<Model>) {
        Log.d(TAG, "onModelsFetched: " + models.size)
        modelLiveData.value = models
    }

    fun loadModels() {
        val catDisposable = mainRepository.currentCategory.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { currentCategory ->
                    val modelDisposable = mainRepository.getModelsByCat(currentCategory.currentCategory)
                            .subscribe({ this.onModelsFetched(it) }, this::onError)
                    compositeDisposable.add(modelDisposable)
                }
        compositeDisposable.add(catDisposable)

    }

    fun setListMapsOfFutureModels(modelList: List<Model>) {

        val returnFutureModelMapList = ArrayList<MutableMap<String, CompletableFuture<ModelRenderable>>>()

        for (i in modelList.indices) {
            val futureMap = mutableMapOf<String,CompletableFuture<ModelRenderable>>()
            futureMap[modelList[i].name] =
                    ModelRenderable.builder().setSource(
                            application, Uri.parse(modelList[i].name + ".sfb")).build()

            returnFutureModelMapList.add(
                    futureMap)
        }

        futureModelMapList.value = returnFutureModelMapList
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
                            Log.e("completable future throwable",throwable.toString())
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

    fun setModelRenderables(futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>){
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
                                Log.e("completable future throwable",throwable.toString())
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

    private fun onError(throwable: Throwable) {
        Log.d("MainViewModel", throwable.message)
    }

    companion object {
        const val TAG = "ArViewModel"
    }
}
