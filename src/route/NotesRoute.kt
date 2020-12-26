package com.gmaniliapp.route

import com.gmaniliapp.data.*
import com.gmaniliapp.data.collection.Note
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
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

                note.owners = mutableListOf(email)
                if (insertNote(note)) {
                    call.respond(HttpStatusCode.OK, note)
                } else {
                    call.respond(HttpStatusCode.InternalServerError, "Error inserting note")
                }
            }
        }
    }
    route("/rest/v1/notes/{noteId}") {
        authenticate {
            put {
                val email = call.principal<UserIdPrincipal>()!!.name
                val bodyNote = try {
                    call.receive<Note>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@put
                }

                val dbNote = selectNoteById(bodyNote.id)
                if (dbNote != null) {
                    if (dbNote.owners.contains(email)) {
                        if (updateNote(bodyNote)) {
                            call.respond(HttpStatusCode.OK, bodyNote)
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Error updating note")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden, "User is not an owner of the note with id = ${bodyNote.id}"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Note not founded")
                }
            }
        }
    }
    route("/rest/v1/notes/{noteId}/owners") {
        authenticate {
            put {
                // TODO: Implement
            }
        }
    }
    route("/rest/v1/notes/sync") {
        authenticate {
            put {
                val email = call.principal<UserIdPrincipal>()!!.name
                val notes = try {
                    call.safeReceive<List<Note>>()
                } catch (exception: ContentTransformationException) {
                    call.respond(HttpStatusCode.BadRequest, "Bad request")
                    return@put
                }

                val result = mutableListOf<Note>()

                if (notes.isNotEmpty()) {
                    notes.forEach { note ->
                        val dbNote = selectNoteById(note.id)
                        if (dbNote != null) {
                            if (dbNote.owners.contains(email)) {
                                if (dbNote.updateDate < note.updateDate) {
                                    updateNote(note)
                                }
                            } else {
                                note.deleted = true
                                result.add(note)
                            }
                        } else {
                            note.owners = mutableListOf(email)
                            if (insertNote(note)) {
                                call.respond(HttpStatusCode.OK, note)
                            } else {
                                call.respond(HttpStatusCode.InternalServerError, "Error inserting note")
                            }
                        }
                    }
                }

                result.addAll(selectNotesByOwner(email))
                call.respond(HttpStatusCode.OK, result)
            }
        }
    }
    route("/rest/v1/notes/{noteId}") {
        authenticate {
            delete {
                val noteId = call.parameters["noteId"]!!
                val email = call.principal<UserIdPrincipal>()!!.name

                val note = selectNoteById(noteId)
                if (note != null) {
                    if (note.owners.contains(email)) {
                        if (updateNoteOwners(note.id, note.owners - email)) {
                            call.respond(HttpStatusCode.OK, "Note successfully deleted")
                        } else {
                            call.respond(HttpStatusCode.InternalServerError, "Error updating note's owners")
                        }
                    } else {
                        call.respond(
                            HttpStatusCode.Forbidden, "User is not an owner of the note"
                        )
                    }
                } else {
                    call.respond(HttpStatusCode.NotFound, "Note not founded")
                }
            }
        }
    }
}

suspend inline fun <reified T> ApplicationCall.safeReceive(): T {
    val json = this.receiveOrNull<String>()
    return Gson().fromJson(json, object : TypeToken<List<Note>>() {}.type)
}