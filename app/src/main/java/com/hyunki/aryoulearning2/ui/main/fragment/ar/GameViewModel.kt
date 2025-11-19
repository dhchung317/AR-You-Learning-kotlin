package com.hyunki.aryoulearning2.ui.main.fragment.ar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.ar.sceneform.rendering.ModelRenderable
import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import java.util.ArrayList
import java.util.Random
import java.util.Stack
import javax.inject.Inject
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.set

data class GamePlacement(
    val modelKey: String,
    val renderable: ModelRenderable
)

class GameViewModel @Inject
constructor() : ViewModel() {
    private val _gamePlacement = MutableLiveData<GamePlacement>()
    private val roundLimit = 3
    private var _attempt: MutableLiveData<String> = MutableLiveData<String>("");
    val attempt: LiveData<String>
        get() = _attempt
    private val _modelMapList = MutableLiveData<List<MutableMap<String, ModelRenderable>>?>()
    val modelMapList: List<MutableMap<String, ModelRenderable>>?
        get() = _modelMapList.value
    fun setModMap(modMap: List<MutableMap<String, ModelRenderable>>) {
        _modelMapList.value = modMap
    }
    private val _letterMap = MutableLiveData<MutableMap<String, ModelRenderable>?>()
    val letterMap: MutableMap<String, ModelRenderable>?
        get() = _letterMap.value
    fun setLetMap(letMap: MutableMap<String, ModelRenderable>) {
        _letterMap.value = letMap
    }
    private val _keyStack = Stack<Model>()
    val keyStack: Stack<Model> get() = _keyStack
    private val _currentWord: MutableLiveData<CurrentWord> = MutableLiveData()
    val currentWord: CurrentWord? get() = _currentWord.value

    var wordHistoryList = ArrayList<CurrentWord>()
        private set

    private val _hasPlacedGame = MutableLiveData<Boolean>()
    val hasPlacedGame: LiveData<Boolean> get() = _hasPlacedGame

    fun setHasPlacedGame(hasPlacedGame: Boolean) {
        _hasPlacedGame.value = hasPlacedGame

        if (hasPlacedGame) {
            val cw = _currentWord.value ?: return
            val entry = getModelEntryFromModelKey(cw.answer) ?: return

            _gamePlacement.value = GamePlacement(
                modelKey = entry.first,
                renderable = entry.second
            )
        }
    }
    private val r = Random()
    private fun getRandom(max: Int): Int {
        val min = 0
        return r.nextInt(max - min) + min
    }

    val combined =
        MediatorLiveData<Triple<List<MutableMap<String, ModelRenderable>>, Boolean, CurrentWord>>()

    init {
        fun update() {
            val mm = _modelMapList.value
            val hp = _hasPlacedGame.value
            val cw = _currentWord.value
            if (mm != null && hp != null && cw != null) {
                combined.value =
                    Triple(mm, hp, cw)
            }
        }
        combined.addSource(_modelMapList) { update() }
        combined.addSource(_hasPlacedGame) { update() }
        combined.addSource(_currentWord) { update() }
    }

    fun ingest(modelList: List<Model>) {
        while (keyStack.size < roundLimit && keyStack.size < modelList.size) {
            val ran = getRandom(modelList.size)

            if (!keyStack.contains(modelList[ran])) {
                _keyStack.add(modelList[ran])
            }
        }
        setNextWord()
    }

    fun setNextWord() {
        if (keyStack.isNotEmpty()) {
            _currentWord.value = CurrentWord(keyStack.pop())
            _attempt.value = ""
        }
    }

    fun addLetterToAttempt(letter: String) {
        val current = _attempt.value ?: ""
        val updated = current + letter
        _attempt.value = updated
    }

    fun removeLetterFromAttempt(): String? {
        val current = _attempt.value ?: return null
        if (current.isEmpty()) return null

        val removed = current.takeLast(1)
        val updated = current.dropLast(1)
        _attempt.value = updated
        return removed
    }

    fun getModelEntryFromModelKey(modelKey: String): Pair<String, ModelRenderable>? {
        val list = modelMapList ?: return null
        val map = list.fold(mutableMapOf<String, ModelRenderable>()) { acc, v ->
            v.forEach { (key, value) -> acc[key] = value }
            acc
        }
        val model = map[modelKey] ?: return null
        return modelKey to model
    }
}