package com.machina

data class HttpResult(
    val code: Int,
    val status: String,
    val contentType: String = "",
    val contentLength: Int = 0,
    val content: Any = "",
    val basicAuth: Int
)
