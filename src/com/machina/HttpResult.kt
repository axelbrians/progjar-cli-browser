package com.machina

data class HttpResult(
    val code: Int,
    val status: String,
    val contentType: String = "",
    val contentLength: Long = 0,
    val contentHeader: Any = "",
    val content: Any = "",
    val basicAuth: Int
)
