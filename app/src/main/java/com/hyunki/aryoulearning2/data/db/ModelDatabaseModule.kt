package com.hyunki.aryoulearning2.data.db

import android.app.Application

import androidx.room.Room

import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao

import dagger.Module
import dagger.Provides

@Module
class ModelDatabaseModule {

    @Provides
    fun provideModelDatabase(application: Application): ModelDatabase {
        return Room.databaseBuilder(
                application.applicationContext,
                ModelDatabase::class.java, ModelDatabase.DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build()
    }

    @Provides
    fun provideModelDao(modelDatabase: ModelDatabase): ModelDao {
        return modelDatabase.modelDao()
    }

    @Provides
    fun provideCatDao(modelDatabase: ModelDatabase): CategoryDao {
        return modelDatabase.catDao()
    }
}