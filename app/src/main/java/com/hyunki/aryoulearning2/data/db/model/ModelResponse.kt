package com.hyunki.aryoulearning2.data.db.model

import java.util.ArrayList

data class ModelResponse(
        val list: ArrayList<Model>,
        val category: String,
        val background: String
)