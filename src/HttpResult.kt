data class HttpResult(
    val code: Int,
    val status: String,
    val refreshUrl: String = "",
    val contentType: String = "",
    val content: Any = "",
)
