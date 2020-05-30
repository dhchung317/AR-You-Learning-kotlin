package com.hyunki.aryoulearning2.ui.main.ar

import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.db.model.ModelResponse
import com.hyunki.aryoulearning2.ui.main.MainState

sealed class ArState {
    object Loading : ArState()

    object Error : ArState()

    sealed class Success : ArState() {

        data class OnModelsLoaded(
                val responses: List<Model>
        ) : Success()

    }

}