package com.hyunki.aryoulearning2.ui.main.fragment.ar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.MainState
import com.hyunki.aryoulearning2.data.ArState
import com.hyunki.aryoulearning2.ui.main.MainViewModel

data class ArGameUiState(
    val attemptText: String = "",
    val isUndoVisible: Boolean = false,
    val isWordCardVisible: Boolean = false,

    // null = nothing to show; true/false = correct/incorrect
    val showResult: Boolean? = null,

    // event for starting a new AR round
    val shouldStartNewRound: Boolean = false,
    val nextModelKey: String? = null,
    val nextRenderable: ModelRenderable? = null
)

class ArGameCoordinatorViewModel(
    private val arViewModel: ArViewModel,
    private val gameViewModel: GameViewModel,
    mainViewModel: MainViewModel
) : ViewModel() {

    private val _uiState = MediatorLiveData<ArGameUiState>().apply {
        value = ArGameUiState()
    }
    val uiState: LiveData<ArGameUiState> get() = _uiState

    init {
        // 1) If MainViewModel ever emits models, also feed pipeline from there
        _uiState.addSource(mainViewModel.modelLiveData) { state ->
            when (state) {
                is MainState.Success.OnModelsLoaded -> {
                    val models = state.models
                    // Feed game logic
                    gameViewModel.ingest(models)
                    // Start AR pipeline from these models
                    arViewModel.loadListofMapsOfFutureModels(models)
                    arViewModel.loadLetterFuturesFromModels(models)
                }

                else -> Unit
            }
        }

        // 2) AR: future model maps -> build renderables & letter futures
        _uiState.addSource(arViewModel.futureModelMapListLiveData) { state ->
            when (state) {
                is ArState.Success.OnFutureModelMapListLoaded -> {
                    val futureList = state.futureModelMapList
                    // Build model renderables
                    arViewModel.loadModelRenderables(futureList)
                }

                else -> Unit
            }
        }

        // 3) AR: future letter map -> build letter renderables
        _uiState.addSource(arViewModel.futureLetterMapLiveData) { state ->
            when (state) {
                is ArState.Success.OnFutureLetterMapLoaded -> {
                    arViewModel.loadLetterRenderables(
                        state.futureLetterMap
                    )
                }

                else -> Unit
            }
        }

        // 4) AR: final model renderables -> push into gameViewModel
        _uiState.addSource(arViewModel.modelMapListLiveData) { state ->
            when (state) {
                is ArState.Success.OnModelMapListLoaded -> {
                    gameViewModel.setModMap(state.modelMap)
                }

                else -> Unit
            }
        }

        // 5) AR: final letter renderables -> push into gameViewModel
        _uiState.addSource(arViewModel.letterMapLiveData) { state ->
            when (state) {
                is ArState.Success.OnLetterMapLoaded -> {
                    gameViewModel.setLetMap(state.letterMap)
                }

                else -> Unit
            }
        }

        // 6) GAME: attempt text + correct/incorrect state + card visibility
        _uiState.addSource(gameViewModel.attempt) { attempt ->
            val currentWord = gameViewModel.currentWord
            val answer = currentWord?.answer

            val shouldShowResult: Boolean? =
                if (answer != null && attempt.length == answer.length) {
                    (attempt == answer)
                } else {
                    null
                }

            val hasText = attempt.isNotEmpty()
            val old = _uiState.value ?: ArGameUiState()

            _uiState.value = old.copy(
                attemptText = attempt,
                isUndoVisible = hasText,
                isWordCardVisible = hasText,
                showResult = shouldShowResult
            )
        }

        // 7) GAME: new-round trigger via combined (modelMapList, hasPlacedGame, currentWord)
        _uiState.addSource(gameViewModel.combined) { (modelMapList, _, currentWord) ->
            // modelMapList is there, but we use the helper for consistency
            val entry = gameViewModel.getModelEntryFromModelKey(currentWord.answer)
            val hasPlacedGame = gameViewModel.hasPlacedGame.value


            val old = _uiState.value ?: ArGameUiState()

            if (entry != null && hasPlacedGame == true) {
                _uiState.value = old.copy(
                    shouldStartNewRound = true,
                    nextModelKey = entry.first,
                    nextRenderable = entry.second
                )
            } else {
                _uiState.value = old.copy(
                    shouldStartNewRound = false,
                    nextModelKey = null,
                    nextRenderable = null
                )
            }
        }
    }

    // Clear one-time result event
    fun consumeResult() {
        val old = _uiState.value ?: return
        _uiState.value = old.copy(showResult = null)
    }

    // Clear one-time round start event
    fun consumeRound() {
        val old = _uiState.value ?: return
        _uiState.value = old.copy(
            shouldStartNewRound = false,
            nextModelKey = null,
            nextRenderable = null
        )
    }
}

class ArGameCoordinatorFactory(
    private val arViewModel: ArViewModel,
    private val gameViewModel: GameViewModel,
    private val mainViewModel: MainViewModel
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ArGameCoordinatorViewModel::class.java)) {
            return ArGameCoordinatorViewModel(arViewModel, gameViewModel, mainViewModel) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class $modelClass")
    }
}