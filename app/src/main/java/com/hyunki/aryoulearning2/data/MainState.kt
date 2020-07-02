package com.hyunki.aryoulearning2.data

import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.data.db.model.ArModelResponse

sealed class MainState {

    object Loading : MainState()

    data class Error(val e: String) : MainState()

    sealed class Success : MainState() {

        data class OnModelResponsesLoaded(
                val responses: List<ArModelResponse>
        ) : Success()

        data class OnModelsLoaded(
                val arModels: List<ArModel>
        ) : Success()

        data class OnCategoriesLoaded(
                val categories: List<Category>
        ) : Success()

    }
}