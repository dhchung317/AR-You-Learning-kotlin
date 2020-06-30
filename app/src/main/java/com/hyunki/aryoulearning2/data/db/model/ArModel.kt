package com.hyunki.aryoulearning2.data.db.model

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class ArModel(
        @PrimaryKey
        val name: String,
        @ColumnInfo(name = "category")
        val category: String,
        @ColumnInfo(name = "image")
        val image: String
)
