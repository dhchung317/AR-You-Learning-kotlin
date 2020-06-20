package com.hyunki.aryoulearning2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hyunki.aryoulearning2.data.db.model.Model

@Dao
interface ModelDao {

    @Query("SELECT name, category, image FROM models WHERE category LIKE :category")
    suspend fun getModelsByCat(category: String): List<Model>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(model: Model): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg models: Model): List<Long>

    @Query("DELETE FROM models")
    fun deleteAll()
}
