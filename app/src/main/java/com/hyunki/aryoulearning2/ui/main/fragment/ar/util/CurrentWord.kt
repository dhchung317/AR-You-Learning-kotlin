package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import com.hyunki.aryoulearning2.data.db.model.Model
import java.util.HashSet

class CurrentWord(val answerModel: Model) {
    val attempts = HashSet<String>()
    val image = answerModel.image
    val answer = answerModel.name
    fun getAttempts(): Set<String> {
        return attempts
    }

    fun addWrongAnswerToSet(incorrectAttempt: String) {
        attempts.add(incorrectAttempt)
    }
}