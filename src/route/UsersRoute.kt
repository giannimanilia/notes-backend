package com.gmaniliapp.route

import com.gmaniliapp.data.checkIfUserExists
import com.gmaniliapp.data.collection.User
import com.gmaniliapp.data.insertUser
import com.gmaniliapp.data.request.AccountRequest
import io.ktor.application.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.usersRoute() {
    route("/rest/v1/users") {
        post {
            val request = try {
                call.receive<AccountRequest>()
            } catch (exception: ContentTransformationException) {
                call.respond(HttpStatusCode.BadRequest, "Bad request")
                return@post
            }

            if (!checkIfUserExists(request.email)) {
                if (insertUser(User(email = request.email, password = request.password))) {
                    call.respond(HttpStatusCode.Created, "User successfully registered")
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Error registering user")
                }
            } else {
                call.respond(
                    HttpStatusCode.NotAcceptable,
                    "There already exists an user associated to that mail"
                )
            }
        }
    }
}