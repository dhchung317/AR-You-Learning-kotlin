package com.hyunki.aryoulearning2.ui.main.fragment.controller

import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.ui.main.fragment.ar.util.CurrentWord

interface NavListener {
    fun moveToListFragment()

    fun moveToGameFragment()

    fun moveToResultsFragment()

    fun moveToHintFragment()

    fun moveToReplayFragment()

    fun saveWordHistoryFromGameFragment(wordHistory: List<CurrentWord>)

    fun moveToTutorialFragment()
}
