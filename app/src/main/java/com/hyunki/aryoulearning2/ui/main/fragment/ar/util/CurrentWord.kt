package com.hyunki.aryoulearning2.ui.main.fragment.ar.util

import com.hyunki.aryoulearning2.data.db.model.Model
import java.util.HashSet

class CurrentWord(answerModel: Model) {
    private val _attempts = HashSet<String>()
    val attempts: HashSet<String> get() = _attempts

    val image = answerModel.image
    val answer = answerModel.name

    // TODO: implement wrong answer handling
    fun addWrongAnswerToSet(incorrectAttempt: String) {
        _attempts.add(incorrectAttempt)
    }
}