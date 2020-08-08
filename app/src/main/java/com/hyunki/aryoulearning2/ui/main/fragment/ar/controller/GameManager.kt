package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ArModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.lang.ref.WeakReference
import java.util.*

class GameManager(private val arModelList: List<ArModel>,
                  private val gameCommandsWeakReference: WeakReference<GameCommandListener>,
                  private val navListenerWeakReference: WeakReference<NavListener>) {

    private var gameCommands = gameCommandsWeakReference.get()
    private var navListener = navListenerWeakReference.get()
    private var managerState: GameManagerState = GameManagerState.Uninitialized
    private val roundLimit = 3

    private lateinit var currentWord: CurrentWord

    val keyStack = Stack<ArModel>()
    var arModelUtil: ArModelUtil = ArModelUtil()
    var wordHistoryList = ArrayList<CurrentWord>()
        private set
    var attempt: String = ""
        private set

    init {
        setupGameManager()
        setManagerState(GameManagerState.Initialized)
        //TODO examine this line
//        keyStack[keyStack.size] = null
    }

    private fun setupGameManager() {
        while (keyStack.size < roundLimit && keyStack.size < arModelList.size) {
            val randomModel = arModelList.random()
            if (!keyStack.contains(randomModel)) {
                keyStack.add(randomModel)
            }
        }
        currentWord = CurrentWord(keyStack.pop())
    }

    fun addTappedLetterToCurrentWordAttempt(letter: String): Boolean {
        addLetterToAttempt(letter)
        return checkIfTappedLetterIsCorrect(letter)
    }

    fun onWordAnswered() {
        if (isWordAnswered()) {
            when (isCorrectAnswer()) {
                true -> showCard(isCorrect = true)
                else -> {
                    showCard(isCorrect = false)
                    recordWrongAnswer(attempt)
                }
            }
        }
    }

    private fun isWordAnswered(): Boolean {
        return attempt.length == currentWord.answer.length
    }

    private fun isCorrectAnswer(): Boolean {
        return attempt == getCurrentWord().answer
    }

    fun onHidingCard(wasAnswerCorrect: Boolean) {
        when (wasAnswerCorrect) {
            true -> onAnswerWasCorrect()
            else -> onAnswerWasIncorrect()
        }
    }

    private fun onAnswerWasIncorrect() {
        startGameFromGameManager(currentWord.answerArModel)
    }

    private fun onAnswerWasCorrect() {
        wordHistoryList.add(currentWord)
        when (areGamesLeft()) {
            true -> onGamesLeft()
            else -> onGamesOver()
        }
    }

    private fun areGamesLeft(): Boolean {
        return keyStack.size > 0
    }

    private fun onGamesLeft() {
        startGameFromGameManager(keyStack.pop())
    }

    private fun onGamesOver() {
        setManagerState(GameManagerState.GameOver)
        navListener?.let{
            it.saveWordHistoryFromGameFragment(wordHistoryList)
            it.moveToReplayFragment()
        }
        navListenerWeakReference.clear()
        navListener = null
        gameCommandsWeakReference.clear()
        gameCommands = null
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter =
                getCurrentWord().answer[attempt.length - 1].toString()
        return tappedLetter == correctLetter
    }

    private fun startGameFromGameManager(key: ArModel) {
        setCurrentWord(key)
        refreshManager()
        gameCommands?.startGame(key.name)
    }

    private fun addLetterToAttempt(letter: String) {
        this.attempt += letter
    }

    fun subtractLetterFromAttempt(): String {
        var letter = ""
        if (attempt.isNotEmpty()) {
            letter = attempt.substring(attempt.length - 1)
            attempt = attempt.substring(0, attempt.length - 1)
        }
        return letter
    }

    private fun recordWrongAnswer(wrongAnswer: String) {
        currentWord.addWrongAnswerToSet(wrongAnswer)
    }

    private fun refreshManager() {
        arModelUtil.refreshCollisionSet()
        attempt = ""
    }

    private fun setManagerState(state: GameManagerState) {
        managerState = state
    }

    private fun setCurrentWord(key: ArModel) {
        if (currentWord.answer != key.name) {
            currentWord = CurrentWord(key)
        }
    }

    private fun showCard(isCorrect: Boolean) {
        gameCommands?.showCard(isCorrect)
    }

    fun getCurrentWord(): CurrentWord {
        return currentWord
    }

    fun getKeyCount(): Int {
        return keyStack.size
    }

    fun isFirstGame(): Boolean {
        return roundLimit == keyStack.size && currentWord.getAttempts().isEmpty()
    }

    fun isGameOverState(): Boolean {
        return managerState == GameManagerState.GameOver
    }
}