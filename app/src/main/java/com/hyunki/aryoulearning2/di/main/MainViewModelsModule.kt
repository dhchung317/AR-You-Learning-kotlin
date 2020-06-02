package com.hyunki.aryoulearning2.di.main

import androidx.lifecycle.ViewModel

import com.hyunki.aryoulearning2.di.ViewModelKey
import com.hyunki.aryoulearning2.ui.main.MainViewModel
import com.hyunki.aryoulearning2.ui.main.fragment.ar.ArViewModel

import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelsModule {

    @Binds
    @IntoMap
    @ViewModelKey(MainViewModel::class)
    abstract fun bindMainViewModel(viewModel: MainViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(ArViewModel::class)
    abstract fun bindArViewModel(viewModel: ArViewModel): ViewModel
}
