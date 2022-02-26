package com.machina

data class HttpUrl(
    val protocol: String = "http",
    val host: String = "",
    val url: String = "",
    val method: String = "GET"
)
