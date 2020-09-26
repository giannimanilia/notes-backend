package com.gmaniliapp.route

import com.gmaniliapp.data.*
import com.gmaniliapp.data.collection.Note
import com.gmaniliapp.data.request.AddOwnerRequest
import com.gmaniliapp.data.request.DeleteNoteRequest
import io.ktor.application.*
import io.ktor.auth.*
import io.ktor.features.ContentTransformationException
import io.ktor.http.*
import io.ktor.request.*
import io.ktor.response.*
import io.ktor.routing.*
import io.netty.util.internal.StringUtil

fun Route.notesRoute() {
    route("/rest/v1/notes") {
        authenticate {
            get {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = selectNotesByOwner(email)
                call.respond(HttpStatusCode.OK, notes)
            }
        }
    }
    route("/rest/v1/notes") {
        authenticate {
            post {
                val email = call.principal<UserIdPrincipal>()!!.name
                val note = try {
                    call.receive<Note>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@post
                }

                if (note.owners.contains(email)) {
                    if (selectNoteById(note.id) == null) {
                        if (insertNote(note)) {
                            call.respond(HttpStatusCode.OK, note)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Error inserting note")
                        }
                    } else {
                        call.respond(HttpStatusCode.NotAcceptable, "Note with id = ${note.id} already exists")
                    }
                } else {
                    call.respond(HttpStatusCode.Forbidden, "User is not an owner of the note")
                }
            }
        }
    }
    route("/rest/v1/notes") {
        authenticate {
            put {
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<Note>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@put
                }

                var note = selectNoteById(request.id)
                if (note != null) {
                    if (note.owners.contains(email)) {
                        note = request
                        if (updateNote(note)) {
                            call.respond(HttpStatusCode.OK, note)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Error updating note")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden, "User is not an owner of the note with id = ${request.id}"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "No note founded with id = ${request.id}")
                }
            }
        }
    }
    route("/rest/v1/notes/{noteId}/owners") {
        authenticate {
            put {
                val noteId = call.parameters["noteId"]!!
                val email = call.principal<UserIdPrincipal>()!!.name
                val request = try {
                    call.receive<AddOwnerRequest>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@put
                }

                if (!StringUtil.isNullOrEmpty(request.owner)) {
                    val note = selectNoteById(noteId)
                    if (note != null) {
                        if (note.owners.contains(email)) {
                            if (!note.owners.contains(request.owner)) {
                                if (updateNoteOwners(noteId, note.owners + request.owner)) {
                                    call.respond(HttpStatusCode.OK, note)
                                } else {
                                    call.respond(HttpStatusCode.InternalServerError, "Error updating note")
                                }
                            } else {
                                call.respond(
                                    HttpStatusCode.NotAcceptable, "User is already an owner of the note with id = $noteId"
                                )
                            }
                        } else {
                            call.respond(HttpStatusCode.Forbidden, "User is not an owner of the note with id = $noteId")
                        }
                    } else {
                        call.respond(HttpStatusCode.NotFound, "No note founded with id = $noteId")
                    }
                } else {
                    call.respond(HttpStatusCode.BadRequest, "Owner is missing")
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
                if (note != null) {
                    if (note.owners.contains(email)) {
                        if (note.owners.size > 1) {
                            if (updateNoteOwners(note.id, note.owners - email)) {
                                call.respond(
                                    HttpStatusCode.OK, "Note successfully deleted for user with email = $email"
                                )
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Error updating note's owners")
                            }
                        } else {
                            if (deleteNote(note.id)) {
                                call.respond(HttpStatusCode.OK, "Note successfully deleted")
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Error deleting note")
                            }
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden, "User is not an owner of the note with id = ${request.id}"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "No note founded with id = ${request.id}")
                }
            }
        }
    }
}