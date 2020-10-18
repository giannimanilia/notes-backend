package com.gmaniliapp.data.collection

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Note(
    @BsonId
    val id: String = ObjectId().toString(),
    val title: String,
    val content: String,
    val date: Long,
    val owners: List<String>,
    val color: String,
    val deleted: Boolean
)