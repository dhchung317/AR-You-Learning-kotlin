package com.hyunki.aryoulearning2.ui.main.ar.util

import java.util.HashSet

class CurrentWord(val answer: String) {
    val attempts = HashSet<String>()

    fun getAttempts(): Set<String> {
        return attempts
    }

    fun addWrongAnswerToSet(incorrectAttempt: String) {
        attempts.add(incorrectAttempt)
    }
}