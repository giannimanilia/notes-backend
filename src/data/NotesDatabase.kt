package com.gmaniliapp.data

import com.gmaniliapp.data.collection.User
import com.gmaniliapp.data.response.StandardResponse
import io.ktor.http.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.reactivestreams.KMongo

private val client = KMongo.createClient().coroutine
private val database = client.getDatabase("NotesDatabase")
private val userCollection = database.getCollection<User>()

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