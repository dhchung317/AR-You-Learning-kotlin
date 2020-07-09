package com.hyunki.aryoulearning2.di

import android.app.Application
import com.hyunki.aryoulearning2.data.MainRepository
import com.hyunki.aryoulearning2.data.MainRepositoryImpl
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ArModelDao
import com.hyunki.aryoulearning2.data.network.main.MainApi
import com.hyunki.aryoulearning2.util.Constants
import com.hyunki.aryoulearning2.util.DefaultDispatcherProvider
import com.hyunki.aryoulearning2.util.DispatcherProvider
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@Module
class AppModule {

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
                .baseUrl(Constants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
    }

    @Provides
    @Singleton
    fun provideMainApi(retrofit: Retrofit): MainApi {
        return retrofit.create(MainApi::class.java)
    }

    @Provides
    @Singleton
    fun providePronunciationUtil(application: Application): PronunciationUtil {
        return PronunciationUtil(application.baseContext)
    }

    @Provides
    @Singleton
    fun provideMainRepository(arModelDao: ArModelDao, categoryDao: CategoryDao, mainApi: MainApi): MainRepository {
        return MainRepositoryImpl(arModelDao, categoryDao, mainApi)
    }

    @Provides
    @Singleton
    fun provideDefaultDispatcher(): DispatcherProvider {
        return DefaultDispatcherProvider()
    }
}

