package com.example.response

import io.ktor.http.*

data class ResponseFail(
    val responseCode: HttpStatusCode,
    val message: String
)