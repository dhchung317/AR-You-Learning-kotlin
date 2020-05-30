package com.hyunki.aryoulearning2.db

import androidx.room.Database
import androidx.room.RoomDatabase

import com.hyunki.aryoulearning2.db.dao.CategoryDao
import com.hyunki.aryoulearning2.db.dao.CurrentCategoryDao
import com.hyunki.aryoulearning2.db.dao.ModelDao
import com.hyunki.aryoulearning2.db.model.Category
import com.hyunki.aryoulearning2.db.model.CurrentCategory
import com.hyunki.aryoulearning2.db.model.Model

@Database(entities = [Model::class, Category::class, CurrentCategory::class], version = 4, exportSchema = false)
abstract class ModelDatabase : RoomDatabase() {

    abstract fun modelDao(): ModelDao

    abstract fun catDao(): CategoryDao

    abstract fun curCatDao(): CurrentCategoryDao

    companion object {
        const val DATABASE_NAME = "data.db"
    }
}