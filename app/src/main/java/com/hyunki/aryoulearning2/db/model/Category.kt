package com.hyunki.aryoulearning2.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
class Category(@field:PrimaryKey
               var name: String, image: String) {

    var image: String
        internal set

    init {
        this.image = image
    }
}