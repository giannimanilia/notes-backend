package com.gmaniliapp.route

import com.gmaniliapp.data.selectNotesByEmail
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.http.*
import io.ktor.response.*
import io.ktor.routing.*

fun Route.notesRoute() {
    route("/rest/v1/notes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = selectNotesByEmail(email)
                call.respond(HttpStatusCode.OK, notes)
            }
        }
    }
}