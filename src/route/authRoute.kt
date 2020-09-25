package com.gmaniliapp.route

import com.gmaniliapp.data.request.AccountRequest
import com.gmaniliapp.data.response.StandardResponse
import com.gmaniliapp.data.selectUserByEmail
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.authRoute() {
    route("/rest/v1/auth/login") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (exception: ContentTransformationException) {
                call.respond(StandardResponse(HttpStatusCode.BadRequest.value, "Bad request"))
                return@post
            }

            val user = selectUserByEmail(request.email)
            if (user != null) {
                if (request.password == user.password) {
                    call.respond(StandardResponse(HttpStatusCode.OK.value, "User successfully logged in"))
                } else {
                    call.respond(StandardResponse(HttpStatusCode.Forbidden.value, "Invalid credentials"))
                }
                return@post
            }
            call.respond(StandardResponse(HttpStatusCode.NotFound.value, "User not founded"))
        }
    }
}