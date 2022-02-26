data class HttpResult(
    val code: Int,
    val status: String,
    val contentType: String = "",
    val content: Any = "",
)
