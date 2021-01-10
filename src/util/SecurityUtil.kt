package com.gmaniliapp.util

import java.security.MessageDigest

object SecurityUtil {
    fun hash(text: String): String {
        val bytes = text.toByteArray()
        val messageDigest = MessageDigest.getInstance("SHA-256")
        val digest = messageDigest.digest(bytes)
        return digest.fold("", { str, it -> str + "%02x".format(it) })
    }
}