package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener
import java.util.*

class GameManager(modelMapKeys: List<String>, private val gameCommands: GameCommandListener, private val navListener: NavListener) {
    private val roundLimit = 3
    val keyStack = Stack<String>()
    var modelUtil: ModelUtil = ModelUtil()

    var wordHistoryList = ArrayList<CurrentWord>()
        private set

    var attempt: String = ""
        private set

    var currentWord: CurrentWord
        private set

    init {
        while (keyStack.size < roundLimit && keyStack.size < modelMapKeys.size) {
            val ran = getRandom(modelMapKeys.size, 0)
            if (!keyStack.contains(modelMapKeys[ran])) {
                keyStack.add(modelMapKeys[ran])
            }
        }
        this.currentWord = CurrentWord(keyStack.pop())
    }

    fun addTappedLetterToCurrentWordAttempt(letter: String): Boolean {
        addLetterToAttempt(letter)
        val isCorrect = checkIfTappedLetterIsCorrect(letter)
//        onWordAnswered()
        return isCorrect
    }

    fun onWordAnswered() {
        if (isWordAnswered()) {
            when (isCorrectAnswer()) {
                true -> {
//                    onAnswerCorrect()
                    showCard(isCorrect = true)
                    //showCard() with correct validators
                }
                else -> {
//                    onAnswerIncorrect()
                    showCard(isCorrect = false)
                    //showCard() with incorrect validators
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

    fun onHidingCard(wasAnswerCorrect: Boolean){
        when (wasAnswerCorrect) {
            //listener from fragment will inform manager which course of action to take
            true -> onAnswerCorrect()

            else -> onAnswerIncorrect()
        }
    }
    private fun onAnswerIncorrect() {
        recordWrongAnswer(attempt)
        startNextGame(currentWord.answer)
    }

    private fun onAnswerCorrect() {
        when (areGamesLeft()) {
            true -> whenGamesLeft()
            else -> whenGamesOver()
        }
    }

    private fun areGamesLeft(): Boolean {
        return keyStack.size > 0
    }

    private fun whenGamesLeft() {
        wordHistoryList.add(currentWord)
        startNextGame(keyStack.pop())
    }

    private fun whenGamesOver() {
        wordHistoryList.add(currentWord)
        navListener.saveWordHistoryFromGameFragment(wordHistoryList)
        navListener.moveToReplayFragment()
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter =
                getCurrentWordAnswer()[attempt.length - 1].toString()
        return tappedLetter == correctLetter
    }

    private fun startNextGame(key: String) {
        refreshManager(key)
        gameCommands.startNextGame(key)
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

    private fun refreshManager(key: String) {
        if (currentWord.answer != key) {
            currentWord = CurrentWord(key)
        }
        modelUtil.refreshCollisionSet()
        attempt = ""
    }

    private fun showCard(isCorrect: Boolean) {
        //pass along data for frgment to show
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