package com.hyunki.aryoulearning2.data.db

import android.app.Application

import androidx.room.Room

import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ArModelDao

import dagger.Module
import dagger.Provides

@Module
class ArModelDatabaseModule {

    @Provides
    fun provideModelDatabase(application: Application): ArModelDatabase {
        return Room.databaseBuilder(
                application.applicationContext,
                ArModelDatabase::class.java, ArModelDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    @Provides
    fun provideArModelDao(arModelDatabase: ArModelDatabase): ArModelDao {
        return arModelDatabase.modelDao()
    }

    @Provides
    fun provideCatDao(arModelDatabase: ArModelDatabase): CategoryDao {
        return arModelDatabase.catDao()
    }
}