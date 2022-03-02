package com.jetbrains.handson.httpapi.models

import kotlinx.serialization.Serializable

val userStorage = mutableListOf(
    User("admin", "1234")
)

@Serializable
data class User(val username: String, val password: String)
