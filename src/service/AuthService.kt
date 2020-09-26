package com.gmaniliapp.service

import com.gmaniliapp.data.response.StandardResponse
import com.gmaniliapp.data.selectUserByEmail
import io.ktor.http.*

suspend fun login(email: String, password: String): StandardResponse {
    val user = selectUserByEmail(email)
    return if (user != null) {
        return if (password == user.password) {
            StandardResponse(HttpStatusCode.OK, user)
        } else {
            StandardResponse(HttpStatusCode.Forbidden, "Invalid credentials")
        }
    } else {
        StandardResponse(HttpStatusCode.NotFound, "User not founded")
    }
}