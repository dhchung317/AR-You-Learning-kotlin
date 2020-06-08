package com.hyunki.aryoulearning2.data.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.hyunki.aryoulearning2.data.db.model.Model

import io.reactivex.Single

@Dao
interface ModelDao {

    @Query("SELECT category, name, image FROM models WHERE category = :category")
    fun getModelsByCat(category: String): Single<List<Model>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(model: Model)

    @Query("DELETE FROM models")
    fun deleteAll()

}
