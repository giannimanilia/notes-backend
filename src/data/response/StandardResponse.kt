package com.gmaniliapp.data.response

import io.ktor.http.*

data class StandardResponse(
    val code: Int,
    val message: String
)