package com.hyunki.aryoulearning2.model

import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey

import java.util.ArrayList

@Entity(tableName = "models")
class Model(
        val category: String,
        @field:PrimaryKey
        val name: String,
        val image: String)
