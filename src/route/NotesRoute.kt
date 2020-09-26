package com.gmaniliapp.route

import com.gmaniliapp.data.*
import com.gmaniliapp.data.collection.Note
import com.gmaniliapp.data.request.DeleteNoteRequest
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
            }
        }
    }
    route("/rest/v1/notes") {
        authenticate {
            delete {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<DeleteNoteRequest>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@delete
                }

                val note = selectNoteById(request.id)
                note?.let {
                    if (it.owners.contains(email)) {
                        if (it.owners.size > 1) {
                            val updateResult = updateNoteOwners(it.id, it.owners - email)
                            if (updateResult) {
                                call.respond(HttpStatusCode.OK, "Note successfully deleted for user with email = $email")
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Error updating note's owners")
                            }
                        } else {
                            val deleteResult = deleteNote(it.id)
                            if (deleteResult) {
                                call.respond(HttpStatusCode.OK, "Note successfully deleted")
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Error deleting note")
                            }
                        }
                    } else {
                        call.respond(HttpStatusCode.Forbidden, "User is not an owner of the note with id = ${request.id}")
                    }
                } ?: call.respond(HttpStatusCode.NotFound, "No note founded with id = ${request.id}")
            }
        }
    }
}