package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

interface GameCommandListener {
    fun startNextGame(modelKey: String)

    fun showCard(isCorrect: Boolean)

    fun onHidingCard(wasCorrect: Boolean)
}