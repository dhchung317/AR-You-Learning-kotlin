package com.hyunki.aryoulearning2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ArModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.ArModel

@Database(version = 3, entities = [ArModel::class, Category::class], exportSchema = false)
abstract class ArModelDatabase : RoomDatabase() {

    abstract fun modelDao(): ArModelDao

    abstract fun catDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "data.db"
    }
}