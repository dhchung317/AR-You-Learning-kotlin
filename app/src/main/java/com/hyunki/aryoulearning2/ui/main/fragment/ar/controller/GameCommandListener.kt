package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

import android.view.View

interface GameCommandListener {
    fun startGame(modelKey: String)

    fun showCard(isCorrect: Boolean)

    fun onHidingCard(wasCorrect: Boolean)
}