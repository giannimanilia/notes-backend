package com.gmaniliapp.data.collection

import org.bson.codecs.pojo.annotations.BsonId
import org.bson.types.ObjectId

data class Note(
    @BsonId
    val id: String = ObjectId().toString(),
    val title: String,
    val content: String,
    val owners: List<String>,
    val color: String,
    val creationDate: Long,
    var updateDate: Long,
    var deleted: Boolean
)