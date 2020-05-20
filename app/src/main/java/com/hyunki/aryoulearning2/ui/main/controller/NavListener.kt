package com.hyunki.aryoulearning2.ui.main.controller

import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.model.Model
import com.hyunki.aryoulearning2.ui.main.ar.util.CurrentWord

interface NavListener {
    fun moveToListFragment()

    fun moveToGameFragment()

    fun moveToResultsFragment()

    fun moveToHintFragment()

    fun moveToReplayFragment()

    fun setWordHistoryFromGameFragment(wordHistory: List<CurrentWord>)

    fun moveToTutorialFragment()

    fun setCategoryFromListFragment(category: Category)
}
