package com.hyunki.aryoulearning2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.hyunki.aryoulearning2.db.model.CurrentCategory

import io.reactivex.Single

@Dao
interface CurrentCategoryDao {
    @get:Query("SELECT * FROM current_category")
    val currentCategory: Single<CurrentCategory>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(currentCategory: CurrentCategory)

    @Query("DELETE FROM current_category")
    fun deleteAll()
}
