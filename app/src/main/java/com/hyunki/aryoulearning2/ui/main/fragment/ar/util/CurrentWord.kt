package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import com.hyunki.aryoulearning2.data.db.model.ArModel
import java.util.HashSet

data class CurrentWord(val answerArModel: ArModel) {
    val attempts = HashSet<String>()
    val image = answerArModel.image
    val answer = answerArModel.name

    fun getAttempts(): Set<String> {
        return attempts
    }

    fun addWrongAnswerToSet(incorrectAttempt: String) {
        attempts.add(incorrectAttempt)
    }
}