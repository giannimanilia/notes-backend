package com.gmaniliapp.route

import com.gmaniliapp.data.request.AccountRequest
import com.gmaniliapp.service.login
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
                call.respond(HttpStatusCode.BadRequest, "Bad request")
                return@post
            }

            val response = login(request.email, request.password)
            call.respond(response.code, response.message)
            return@post
        }
    }
}