package com.hyunki.aryoulearning2.data

import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.db.model.Model
import java.util.concurrent.CompletableFuture

sealed class ArState {

    object Loading : ArState()

    object Error : ArState()

    sealed class Success : ArState() {

        data class OnModelsLoaded(
                val models: List<Model>
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