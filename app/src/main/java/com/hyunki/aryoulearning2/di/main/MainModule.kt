package com.hyunki.aryoulearning2.di.main

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.animation.LottieHelper
import com.hyunki.aryoulearning2.network.main.MainApi
import com.hyunki.aryoulearning2.ui.main.hint.rv.HintAdapter
import com.hyunki.aryoulearning2.ui.main.list.rv.ListAdapter
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @Provides
    fun provideThemeID(): Int {
        return R.style.AppTheme
    }

    @Provides
    fun provideCategoryAdapter(): ListAdapter {
        return ListAdapter()
    }

    @Provides
    fun provideHintAdapter(pronunciationUtil: PronunciationUtil): HintAdapter {
        return HintAdapter(pronunciationUtil)
    }

    @Provides
    fun provideLottieHelper(): LottieHelper {
        return LottieHelper()
    }
}
