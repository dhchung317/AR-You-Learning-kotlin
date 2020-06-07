package com.hyunki.aryoulearning2.di.main

import com.hyunki.aryoulearning2.R
import com.hyunki.aryoulearning2.animation.LottieHelper
import com.hyunki.aryoulearning2.ui.main.fragment.hint.rv.HintAdapter
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import dagger.Module
import dagger.Provides

@Module
class MainModule {

    @Provides
    fun provideThemeID(): Int {
        return R.style.AppTheme
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
