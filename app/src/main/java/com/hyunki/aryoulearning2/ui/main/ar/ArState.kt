package com.hyunki.aryoulearning2.ui.main.ar

import androidx.lifecycle.MutableLiveData
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.MainState
import io.reactivex.Single
import java.util.HashMap
import java.util.concurrent.CompletableFuture

sealed class ArState {
    object Loading : ArState()

    object Error : ArState()

    sealed class Success : ArState() {

        data class OnModelsLoaded(
                val responses: List<Model>
        ) : Success()

        data class OnFutureModelMapListLoaded(
                val futureModelMapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>
        ) : Success()

        data class OnFutureLetterMapLoaded(
                val futureLetterMap: MutableMap<String, CompletableFuture<ModelRenderable>>
        ) : Success()

        data class OnLetterMapLoaded(
                val letterMap: MutableMap<String, ModelRenderable>
        ) : Success()

        data class OnModelMapListLoaded(
                val modelMap: List<MutableMap<String,ModelRenderable>>
        ) : Success()
    }

}