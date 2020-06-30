package com.hyunki.aryoulearning2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.hyunki.aryoulearning2.data.db.model.ArModel

@Dao
interface ArModelDao {

    @Query("SELECT name, category, image FROM models WHERE category LIKE :category")
    suspend fun getModelsByCat(category: String): List<ArModel>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(arModel: ArModel): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg arModels: ArModel): List<Long>

    @Query("DELETE FROM models")
    fun deleteAll()
}
