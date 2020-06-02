package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import java.util.ArrayList
import java.util.Random
import java.util.Stack

class GameManager(modelMapKeys: List<String>, private val gameCommands: GameCommandListener, private val navListener: NavListener) {
    private var currentWord: CurrentWord
    private val roundLimit = 3
    private val keyStack = Stack<String>()
    private val wordHistoryList = ArrayList<CurrentWord>()
    var attempt = ""
        private set

    val currentWordAnswer: String
        get() = currentWord.answer

    init {
        while (keyStack.size < roundLimit) {
            val ran = getRandom(modelMapKeys.size, 0)
            if (!keyStack.contains(modelMapKeys[ran])) {
                keyStack.add(modelMapKeys[ran])
            }
        }
        this.currentWord = CurrentWord(keyStack.pop())
    }

    fun setCurrentWord(currentWord: CurrentWord) {
        this.currentWord = currentWord
    }

    fun addTappedLetterToCurrentWordAttempt(letter: String) {


        if (attempt.length == currentWordAnswer.length) {
            if (attempt.toLowerCase() != currentWordAnswer.toLowerCase()) {
                recordWrongAnswer(attempt)
                startNextGame(currentWord.answer)
            } else {
                if (keyStack.size > 0) {
                    wordHistoryList.add(currentWord)
                    startNextGame(keyStack.pop())
                } else {
                    wordHistoryList.add(currentWord)
                    navListener.setWordHistoryFromGameFragment(wordHistoryList)
                    navListener.moveToReplayFragment()
                }
            }
        }
    }

    fun recordWrongAnswer(wrongAnswer: String) {
        currentWord.addWrongAnswerToSet(wrongAnswer)
    }

    fun startNextGame(key: String) {
        refreshManager(key)
        gameCommands.startNextGame(key)
    }

    fun addLetterToAttempt(letter: String) {
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

    fun refreshManager(key: String) {
        if (currentWord.answer != key) {
            setCurrentWord(CurrentWord(key))
        }
        ModelUtil.refreshCollisionSet()
        attempt = ""
    }

    companion object {
        private val r = Random()

        private fun getRandom(max: Int, min: Int): Int {
            return r.nextInt(max - min) + min
        }
    }
}