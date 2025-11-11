package com.hyunki.aryoulearning2.data.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "models")
data class Model(
    var category: String,
    @field:PrimaryKey
        val name: String,
    val image: String
)
