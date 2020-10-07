package com.example.response

import io.ktor.http.*

data class RegisterUser(
    val responseCode: HttpStatusCode,
    val message: String
)