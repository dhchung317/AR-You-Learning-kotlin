package com.hyunki.aryoulearning2.di

import android.app.Application

import com.hyunki.aryoulearning2.BaseApplication
import com.hyunki.aryoulearning2.data.db.ArModelDatabaseModule
import com.hyunki.aryoulearning2.di.main.MainModule
import com.hyunki.aryoulearning2.di.main.MainViewModelsModule
import com.hyunki.aryoulearning2.ui.main.MainActivity
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArHostFragment
import com.hyunki.aryoulearning2.ui.main.fragment.hint.HintFragment
import com.hyunki.aryoulearning2.ui.main.fragment.category.CategoryFragment
import com.hyunki.aryoulearning2.ui.main.fragment.replay.ReplayFragment
import com.hyunki.aryoulearning2.ui.main.fragment.results.ResultsFragment
import com.hyunki.aryoulearning2.ui.main.fragment.tutorial.TutorialFragment

import javax.inject.Singleton

import dagger.BindsInstance
import dagger.Component

@Component(
        modules = [
            AppModule::class,
            ViewModelFactoryModule::class,
            ArModelDatabaseModule::class,
            MainModule::class,
            MainViewModelsModule::class
        ]
)
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

    fun inject(categoryFragment: CategoryFragment)
    fun inject(arHostFragment: ArHostFragment)
    fun inject(replayFragment: ReplayFragment)
    fun inject(resultsFragment: ResultsFragment)
    fun inject(hintFragment: HintFragment)
    fun inject(tutorialFragment: TutorialFragment)
}