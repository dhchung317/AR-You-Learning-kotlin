package com.hyunki.aryoulearning2.data.db.model

import java.util.ArrayList

data class ArModelResponse(
        val list: ArrayList<ArModel>,
        val category: String,
        val background: String
)