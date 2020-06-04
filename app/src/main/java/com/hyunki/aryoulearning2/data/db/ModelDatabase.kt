package com.hyunki.aryoulearning2.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.hyunki.aryoulearning2.data.db.dao.CategoryDao
import com.hyunki.aryoulearning2.data.db.dao.ModelDao
import com.hyunki.aryoulearning2.data.db.model.Category
import com.hyunki.aryoulearning2.data.db.model.Model

@Database(entities = [Model::class, Category::class], version = 1, exportSchema = false)
abstract class ModelDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao

    abstract fun catDao(): CategoryDao

    companion object {
        const val DATABASE_NAME = "data.db"
    }
}