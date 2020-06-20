package com.hyunki.aryoulearning2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model

@Database(version = 3, entities = [Model::class, Category::class], exportSchema = false)
abstract class ModelDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao

    abstract fun catDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "data.db"
    }
}