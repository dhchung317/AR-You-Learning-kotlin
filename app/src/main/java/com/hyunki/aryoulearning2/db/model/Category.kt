package com.hyunki.aryoulearning2.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class Category(
        @field:PrimaryKey val name: String,
        val image: String)
