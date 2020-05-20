package com.hyunki.aryoulearning2.di

import android.app.Application

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.db.ModelDatabaseModule
import com.hyunki.aryoulearning2.di.main.MainModule
import com.hyunki.aryoulearning2.di.main.MainViewModelsModule
import com.hyunki.aryoulearning2.network.main.MainApi
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.MainRepository
import com.hyunki.aryoulearning2.ui.main.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.hint.HintFragment
import com.hyunki.aryoulearning2.ui.main.list.ListFragment
import com.hyunki.aryoulearning2.ui.main.replay.ReplayFragment
import com.hyunki.aryoulearning2.ui.main.results.ResultsFragment
import com.hyunki.aryoulearning2.ui.main.tutorial.TutorialFragment

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component

@Component(modules = [AppModule::class, ViewModelFactoryModule::class, ModelDatabaseModule::class, MainModule::class, MainViewModelsModule::class])
@Singleton
interface AppComponent {

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun inject(baseApplication: BaseApplication)

    fun inject(mainActivity: MainActivity)

    fun inject(listFragment: ListFragment)
    fun inject(arHostFragment: ArHostFragment)
    fun inject(replayFragment: ReplayFragment)
    fun inject(resultsFragment: ResultsFragment)
    fun inject(hintFragment: HintFragment)
    fun inject(tutorialFragment: TutorialFragment)
}