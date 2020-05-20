package com.hyunki.aryoulearning2.model

import java.util.ArrayList

data class ModelResponse(
        val list: ArrayList<Model>,
        val category: String,
        val background: String
)