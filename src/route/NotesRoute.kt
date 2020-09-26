package com.gmaniliapp.route

import com.gmaniliapp.data.collection.Note
import com.gmaniliapp.data.insertNote
import com.gmaniliapp.data.selectNoteById
import com.gmaniliapp.data.selectNotesByEmail
import com.gmaniliapp.data.updateNote
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
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
    route("/rest/v1/notes") {
        authenticate {
            post {
                val note = try {
                    call.receive<Note>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@post
                }

                if (selectNoteById(note.id) == null) {
                    if (insertNote(note)) {
                        call.respond(HttpStatusCode.OK, note)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Error inserting note")
                    }
                } else {
                    call.respond(HttpStatusCode.NotAcceptable, "Note with id = ${note.id} already exists")
                }
                return@post
            }
        }
    }
    route("/rest/v1/notes") {
        authenticate {
            put {
                val note = try {
                    call.receive<Note>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@put
                }

                if (selectNoteById(note.id) != null) {
                    if (updateNote(note)) {
                        call.respond(HttpStatusCode.OK, note)
                    } else {
                        call.respond(HttpStatusCode.InternalServerError, "Error updating note")
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "No note founded with id = ${note.id}")
                }
                return@put
            }
        }
    }
}