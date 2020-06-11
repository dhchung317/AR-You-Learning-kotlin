package com.hyunki.aryoulearning2.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.hyunki.aryoulearning2.data.db.model.Model

import io.reactivex.Single

@Dao
interface ModelDao {

    @Query("SELECT name, category, image FROM models WHERE category LIKE :category")
    fun getModelsByCat(category: String): LiveData<List<Model>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(model: Model): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(vararg models: Model): List<Long>

    @Query("DELETE FROM models")
    fun deleteAll()

    @Query("SELECT COUNT(name) FROM models")
    fun checkSize(): Int

}
