package com.hyunki.aryoulearning2.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.hyunki.aryoulearning2.db.model.Category

import java.util.ArrayList

import io.reactivex.Single

@Dao
interface CategoryDao {

    @get:Query("SELECT * FROM category")
    val allCategories: Single<List<Category>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(category: Category)

    @Query("DELETE FROM category")
    fun deleteAll()
}
