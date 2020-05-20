package com.hyunki.aryoulearning2.di

import android.app.Application
import com.hyunki.aryoulearning2.network.main.MainApi
import com.hyunki.aryoulearning2.util.Constants
import com.hyunki.aryoulearning2.util.audio.PronunciationUtil

import javax.inject.Singleton

import dagger.Module
import dagger.Provides
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
object AppModule {

    @Provides
    @Singleton
    internal fun provideRetrofit(): Retrofit {
        return Retrofit.Builder().baseUrl(Constants.BASE_URL)
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .addConverterFactory(GsonConverterFactory.create()).build()
    }

    @Provides
    @Singleton
    internal fun provideMainApi(retrofit: Retrofit): MainApi {
        return retrofit.create(MainApi::class.java)
    }

    //    @Provides
    //    @Singleton
    //    static Application provideApplicationContext(Application application) {
    //        return application;
    //    }

    @Provides
    @Singleton
    internal fun providePronunciationUtil(application: Application): PronunciationUtil {
        return PronunciationUtil(application.baseContext)
    }


}

