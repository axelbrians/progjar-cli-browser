import java.io.BufferedInputStream
import java.io.BufferedReader

object HttpHeaderParser {

    private fun parseCode(header: String): Int {
        return header.split(" ")[1].toInt()
    }

    private fun parseStatus(header: String): String {
        return when (parseCode(header)) {
            100 -> "Continue the request or ignore the response if the request is already finished."
            101 -> "This code is sent in response to an Upgrade request header from the client and indicates the protocol the server is switching to."
            102 -> "Processing the request, but no response is available yet."
            103 -> "Start preloading resource while the server prepares a response."

            200 -> "The request succeed."
            201 -> "The request succeeded, and a new resource was created as a result."
            202 -> "The request has been received but not yet acted upon. It is noncommittal, since there is no way in HTTP to later send an asynchronous response indicating the outcome of the request. It is intended for cases where another process or server handles the request, or for batch processing."
            203 -> "This response code means the returned metadata is not exactly the same as is available from the origin server, but is collected from a local or a third-party copy."
            204 -> "here is no content to send for this request, but the headers may be useful. The user agent may update its cached headers for this resource with the new ones."
            else -> ""
        }
    }

    fun parseHeader(buffer: BufferedReader): HttpResult {
        println("reading bytes")

        var lineOfString: String? = ""
        var response = ""
        while (lineOfString != null) {
            lineOfString = buffer.readLine()
            println(lineOfString)
            response += lineOfString + "\n"

            if (lineOfString.isBlank()) {
                break
            }
        }
        println("read header complete")

        val code = parseCode(response.substringBefore("\n"))
        val status = parseStatus(response.substringBefore("\n"))
        val contentType = response.substringAfter("Content-Type: ").substringBefore("\n")

        return HttpResult(
            code = code,
            status = status,
            contentType = contentType,
            content = response)
    }

    fun parseHeader(buffer: BufferedInputStream): HttpResult {
        println("reading bytes")

        var response = ""
        var byte = buffer.readNBytes(100)
        while (byte != null) {
//            println(String(byte))
            byte = buffer.readNBytes(100)
            response += String(byte)
        }
        println("read buffer complete")

        val code = parseCode(response.substringBefore("\n"))
        val status = parseStatus(response.substringBefore("\n"))
        return HttpResult(
            code = code,
            status = status,
            content = response)
    }
}