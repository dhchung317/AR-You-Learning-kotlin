package com.hyunki.aryoulearning2.db.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "current_category")
class CurrentCategory(currentCategory: String) {

    @PrimaryKey
    var CURRENT: String

    var currentCategory: String
        internal set

    init {
        this.CURRENT = "current"
        this.currentCategory = currentCategory
    }

}
