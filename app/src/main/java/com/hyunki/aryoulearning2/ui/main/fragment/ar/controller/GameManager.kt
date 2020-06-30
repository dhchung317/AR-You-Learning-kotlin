package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.data.db.model.ArModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ArModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.util.*

class GameManager(arModelList: List<ArModel>, private val gameCommands: GameCommandListener, private val navListener: NavListener) {
    private val roundLimit = 3

    val keyStack = Stack<ArModel>()

    var arModelUtil: ArModelUtil = ArModelUtil()

    var wordHistoryList = ArrayList<CurrentWord>()
        private set

    var attempt: String = ""
        private set

    var currentWord: CurrentWord
        private set

    init {
        while (keyStack.size < roundLimit && keyStack.size < arModelList.size) {
            val ran = getRandom(arModelList.size, 0)
            if (!keyStack.contains(arModelList[ran])) {
                keyStack.add(arModelList[ran])
            }
        }
        this.currentWord = CurrentWord(keyStack.pop())
        //TODO examine this line
        keyStack[keyStack.size] = null
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
        return attempt == getCurrentWordAnswer()
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
        navListener.saveWordHistoryFromGameFragment(wordHistoryList)
        navListener.moveToReplayFragment()
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter =
                getCurrentWordAnswer()[attempt.length - 1].toString()
        return tappedLetter == correctLetter
    }

    private fun startGameFromGameManager(key: ArModel) {
        refreshManager(key)
        gameCommands.startGame(key.name)
    }

    private fun addLetterToAttempt(letter: String) {
        this.attempt += letter
    }

    fun subtractLetterFromAttempt(): String {
        var letter = ""
        if (!attempt.isEmpty()) {
            letter = attempt.substring(attempt.length - 1)
            attempt = attempt.substring(0, attempt.length - 1)
        }
        return letter
    }

    private fun recordWrongAnswer(wrongAnswer: String) {
        currentWord.addWrongAnswerToSet(wrongAnswer)
    }

    private fun refreshManager(key: ArModel) {
        if (currentWord.answer != key.name) {
            currentWord = CurrentWord(key)
        }
        arModelUtil.refreshCollisionSet()
        attempt = ""
    }

    private fun showCard(isCorrect: Boolean) {
        gameCommands.showCard(isCorrect)
    }

    fun getCurrentWordAnswer(): String {
        return currentWord.answer
    }

    fun getKeyCount(): Int {
        return keyStack.size
    }

    fun isFirstGame():Boolean {
        return roundLimit == keyStack.size - 1
    }

    companion object {
        private val r = Random()
        private fun getRandom(max: Int, min: Int): Int {
            return r.nextInt(max - min) + min
        }
    }
}