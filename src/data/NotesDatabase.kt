package com.gmaniliapp.data

import com.gmaniliapp.data.collection.Note
import com.gmaniliapp.data.collection.User
import org.litote.kmongo.contains
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.setValue

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NOTES_APP")
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
suspend fun selectNotesByOwner(email: String): List<Note> {
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
    note.updateDate = System.currentTimeMillis()

    val dbNote = selectNoteById(note.id)
    if (dbNote != null) {
        note.owners = (dbNote.owners + note.owners).toMutableList()
    }

    return notesCollection.updateOneById(note.id, note).wasAcknowledged()
}

/**
 * Update note's owners
 */
suspend fun updateNoteOwners(id: String, owners: List<String>): Boolean {
    return notesCollection.updateOne(Note::id eq id, setValue(Note::owners, owners)).wasAcknowledged()
}

/**
 * Delete a note
 */
suspend fun deleteNote(id: String): Boolean {
    return notesCollection.deleteOneById(id).wasAcknowledged()
}