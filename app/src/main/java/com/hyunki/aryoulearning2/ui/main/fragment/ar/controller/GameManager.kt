package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.data.db.model.Model
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.util.*

class GameManager(modelList: List<Model>, private val gameCommands: GameCommandListener, private val navListener: NavListener) {
    private val roundLimit = 3

    val keyStack = Stack<Model>()

    var modelUtil: ModelUtil = ModelUtil()

    var wordHistoryList = ArrayList<CurrentWord>()
        private set

    var attempt: String = ""
        private set

    var currentWord: CurrentWord
        private set

    init {
        while (keyStack.size < roundLimit && keyStack.size < modelList.size) {
            val ran = getRandom(modelList.size, 0)
            if (!keyStack.contains(modelList[ran])) {
                keyStack.add(modelList[ran])
            }
        }
        this.currentWord = CurrentWord(keyStack.pop())
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
        startNextGame(currentWord.answerModel)
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
        startNextGame(keyStack.pop())
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

    private fun startNextGame(key: Model) {
        refreshManager(key)
        gameCommands.startNextGame(key.name)
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

    private fun refreshManager(key: Model) {
        if (currentWord.answer != key.name) {
            currentWord = CurrentWord(key)
        }
        modelUtil.refreshCollisionSet()
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

    companion object {
        private val r = Random()
        private fun getRandom(max: Int, min: Int): Int {
            return r.nextInt(max - min) + min
        }
    }
}