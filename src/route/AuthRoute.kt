package com.gmaniliapp.route

import com.gmaniliapp.data.checkIfUserExists
import com.gmaniliapp.data.collection.User
import com.gmaniliapp.data.insertUser
import com.gmaniliapp.data.request.AccountRequest
import com.gmaniliapp.data.response.StandardResponse
import com.gmaniliapp.service.login
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.authRoute() {
    route("/rest/v1/auth/register") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (exception: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, StandardResponse(HttpStatusCode.BadRequest, "Bad request"))
                return@post
            }

            if (!checkIfUserExists(request.email)) {
                if (insertUser(User(email = request.email, password = request.password))) {
                    call.respond(
                        HttpStatusCode.Created,
                        StandardResponse(HttpStatusCode.Created, "User successfully registered")
                    )
                } else {
                    call.respond(
                        HttpStatusCode.InternalServerError,
                        StandardResponse(HttpStatusCode.InternalServerError, "Error inserting user")
                    )
                }
            } else {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    StandardResponse(
                        HttpStatusCode.NotAcceptable,
                        "There already exists an user associated to that mail"
                    )
                )
            }
        }
    }
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
        }
    }
}