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
            204 -> "There is no content to send for this request, but the headers may be useful. The user agent may update its cached headers for this resource with the new ones."
            205 -> "Reset the document which sent this request."
            206 -> "This response code is used when the 'Range' header is sent from the client to request only part of a resource."
            207 -> "Conveys information about multiple resources, for situations where multiple status codes might be appropriate."
            208 -> "Used inside a <dav:propstat> response element to avoid repeatedly enumerating the internal members of multiple bindings to the same collection."
            209 -> "The server has fulfilled a GET request for the resource, and the response is a representation of the result of one or more instance-manipulations applied to the current instance."

            300 -> "The user agent or user should choose one of available options."
            301 -> "The URL of the requested resource has been changed permanently. The new URL is given in the response."
            302 -> "This response code means that the URI of requested resource has been changed temporarily. Further changes in the URI might be made in the future. Therefore, this same URI should be used by the client in future requests."
            303 -> "The server sent this response to direct the client to get the requested resource at another URI with a GET request."
            304 -> "This is used for caching purposes. It tells the client that the response has not been modified, so the client can continue to use the same cached version of the response."
            305 -> "Defined in a previous version of the HTTP specification to indicate that a requested response must be accessed by a proxy. It has been deprecated due to security concerns regarding in-band configuration of a proxy."
            306 -> "This response code is no longer used; it is just reserved. It was used in a previous version of the HTTP/1.1 specification."
            307 -> "The server sends this response to direct the client to get the requested resource at another URI with same method that was used in the prior request. This has the same semantics as the 302 Found HTTP response code, with the exception that the user agent must not change the HTTP method used: if a POST was used in the first request, a POST must be used in the second request."
            308 -> "This means that the resource is now permanently located at another URI, specified by the Location: HTTP Response header. This has the same semantics as the 301 Moved Permanently HTTP response code, with the exception that the user agent must not change the HTTP method used: if a POST was used in the first request, a POST must be used in the second request."

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