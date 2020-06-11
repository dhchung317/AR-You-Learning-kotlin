package com.hyunki.aryoulearning2.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query

import com.hyunki.aryoulearning2.data.db.model.Category

import io.reactivex.Single

@Dao
interface CategoryDao {

    @get:Query("SELECT * FROM category")
    val allCategories: LiveData<List<Category>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(category: Category)

    @Query("DELETE FROM category")
    fun deleteAll()
}
