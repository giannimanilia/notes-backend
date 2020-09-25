package com.gmaniliapp.route

import com.gmaniliapp.data.checkIfUserExists
import com.gmaniliapp.data.collection.User
import com.gmaniliapp.data.insertUser
import com.gmaniliapp.data.request.AccountRequest
import com.gmaniliapp.data.response.StandardResponse
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.userRoute() {
    route("/rest/v1/users") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (exception: ContentTransformationException) {
                call.respond(StandardResponse(HttpStatusCode.BadRequest.value, "Bad request"))
                return@post
            }

            if (!checkIfUserExists(request.email)) {
                if (insertUser(User(email = request.email, password = request.password))) {
                    call.respond(StandardResponse(HttpStatusCode.Created.value, "User successfully registered"))
                } else {
                    call.respond(StandardResponse(HttpStatusCode.InternalServerError.value, "Error registering user"))
                }
            } else {
                call.respond(
                    StandardResponse(
                        HttpStatusCode.NotAcceptable.value,
                        "There already exists an user associated to that mail"
                    )
                )
            }
        }
    }
}