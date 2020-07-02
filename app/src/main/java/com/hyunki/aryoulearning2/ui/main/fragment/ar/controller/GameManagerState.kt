package com.hyunki.aryoulearning2.ui.main.fragment.ar.controller

sealed class GameManagerState {
    object Uninitialized : GameManagerState()
    object Initialized : GameManagerState()
    object GameOver : GameManagerState()
}