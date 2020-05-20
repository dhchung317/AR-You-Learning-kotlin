package com.hyunki.aryoulearning2.di

import androidx.lifecycle.ViewModelProvider

import com.hyunki.aryoulearning2.viewmodel.ViewModelProviderFactory

import dagger.Binds
import dagger.Module

@Module
abstract class ViewModelFactoryModule {
    @Binds
    abstract fun bindViewModelProviderFactory(viewModelProviderFactory: ViewModelProviderFactory): ViewModelProvider.Factory

}
