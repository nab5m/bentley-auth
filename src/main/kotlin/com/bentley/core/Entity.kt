package com.bentley.core

import com.fasterxml.jackson.annotation.JsonIgnore
import java.io.Serializable
import java.time.LocalDateTime

@NoArg
abstract class Entity: Serializable {
    var id: Long
        get() = _id ?: throw UninitializedPropertyAccessException("ID is not initialized")
        set(value) {
            _id = value
        }
    @JsonIgnore
    var _id: Long? = null
    var createdAt: LocalDateTime = LocalDateTime.now()
    var updatedAt: LocalDateTime = LocalDateTime.now()
}