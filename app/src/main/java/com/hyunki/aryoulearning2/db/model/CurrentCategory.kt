package com.hyunki.aryoulearning2.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_category")
data class CurrentCategory(
        @field:PrimaryKey val currentCategory: String
)
//TODO: implement better design of logic to keep track of the current category
