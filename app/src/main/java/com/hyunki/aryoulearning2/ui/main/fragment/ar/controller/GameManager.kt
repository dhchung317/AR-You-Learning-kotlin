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
        val isCorrect = checkIfTappedLetterIsCorrect(letter)
        addLetterToAttempt(letter)
//TODO logic would benefit from some sort of livedata/observable/listener implementation
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
        return isCorrect
    }

    private fun checkIfTappedLetterIsCorrect(tappedLetter: String): Boolean {
        val correctLetter =
                getCurrentWordAnswer()[attempt.length - 1].toString()
        return tappedLetter.toLowerCase(Locale.getDefault()) == correctLetter.toLowerCase(Locale.getDefault())
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