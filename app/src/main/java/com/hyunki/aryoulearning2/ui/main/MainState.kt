package com.hyunki.aryoulearning2.ui.main

import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.Model
import com.hyunki.aryoulearning2.db.model.ModelResponse

sealed class MainState {

    object Loading : MainState()

    object Error : MainState()

    sealed class Success : MainState() {

        data class OnModelResponsesLoaded(
                val responses: List<ModelResponse>
        ) : Success()

        data class OnModelsLoaded(
                val models: List<Model>
        ) : Success()

        data class OnCategoriesLoaded(
                val categories: List<Category>
        ) : Success()

        data class OnCurrentCategoryStringLoaded(
                val currentCategoryString: String
        ) : Success()
    }
}