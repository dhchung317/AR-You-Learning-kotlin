package com.hyunki.aryoulearning2

import java.util.*

class GameManagerTest {
    companion object {
        private val r = Random()
        private fun getRandom(max: Int, min: Int): Int {
            return r.nextInt(max - min) + min
        }
    }
}