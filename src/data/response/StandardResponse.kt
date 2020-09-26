package com.gmaniliapp.data.response

import io.ktor.http.*

data class StandardResponse(
    val code: HttpStatusCode,
    val message: Any
) {
    fun isOk(): Boolean {
        return code == HttpStatusCode.OK || code == HttpStatusCode.Created || code == HttpStatusCode.Accepted
    }
}

