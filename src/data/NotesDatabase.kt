package com.gmaniliapp.data

import com.gmaniliapp.data.collection.Note
import com.gmaniliapp.data.collection.User
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.insertOne
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NotesDatabase")
private val userCollection = database.getCollection<User>()
private val notesCollection = database.getCollection<Note>()

/**
 * Insert an user
 */
suspend fun insertUser(user: User): Boolean {
    return userCollection.insertOne(user).wasAcknowledged()
}

/**
 * Select one user by email
 */
suspend fun selectUserByEmail(email: String): User? {
    return userCollection.findOne(User::email eq email)
}

/**
 * Check if a user exists based on the email
 */
suspend fun checkIfUserExists(email: String): Boolean {
    return selectUserByEmail(email) != null
}

/**
 * Get notes given an email
 */
suspend fun selectNotesByEmail(email: String): List<Note> {
    return notesCollection.find(Note::owners contains email).toList()
}

/**
 * Select a note given an id
 */
suspend fun selectNoteById(id: String): Note? {
    return notesCollection.findOneById(id)
}

/**
 * Insert a note
 */
suspend fun insertNote(note: Note): Boolean {
    return notesCollection.insertOne(note).wasAcknowledged()
}

/**
 * Update a note
 */
suspend fun updateNote(note: Note): Boolean {
    return notesCollection.updateOneById(note.id, note).wasAcknowledged()
}