package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.ModelUtil
import com.hyunki.aryoulearning2.ui.main.fragment.controller.NavListener

import java.util.ArrayList
import java.util.Random
import java.util.Stack

class GameManager(modelMapKeys: List<String>, private val gameCommands: GameCommandListener, private val navListener: NavListener) {
    var currentWord: CurrentWord
        private set
    private val roundLimit = 3

    val keyStack = Stack<String>()

    var wordHistoryList = ArrayList<CurrentWord>()
        private set
    var attempt: String = ""
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

    fun addTappedLetterToCurrentWordAttempt(letter: String) {
        addLetterToAttempt(letter)

        if (attempt.length == getCurrentWordAnswer().length) {
            if (attempt.toLowerCase() != getCurrentWordAnswer().toLowerCase()) {
                recordWrongAnswer(attempt)
                startNextGame(currentWord.answer)
            } else {
                if (keyStack.size > 0) {
                    wordHistoryList.add(currentWord)
                    startNextGame(keyStack.pop())
                } else {
                    wordHistoryList.add(currentWord)
                    navListener.saveWordHistoryFromGameFragment(wordHistoryList)
                    navListener.moveToReplayFragment()
                }
            }
        }
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
        ModelUtil.refreshCollisionSet()
        attempt = ""
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