package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.data.db.model.ModelResponse

sealed class MainState {

    object Loading : MainState()

    object Error : MainState()

    sealed class Success : MainState() {

        data class OnModelResponsesLoaded(
                val responses: ArrayList<ModelResponse>
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