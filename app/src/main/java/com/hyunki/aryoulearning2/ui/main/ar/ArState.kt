package com.hyunki.aryoulearning2.ui.main.ar

import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.MainState
import io.reactivex.Single
import java.util.concurrent.CompletableFuture

sealed class ArState {
    object Loading : ArState()

    object Error : ArState()

    sealed class Success : ArState() {

        data class OnModelsLoaded(
                val responses: List<Model>
        ) : Success()

        data class OnFutureModelMapListLoaded(
                val mapList: List<MutableMap<String, CompletableFuture<ModelRenderable>>>
        ) : Success()

    }

}