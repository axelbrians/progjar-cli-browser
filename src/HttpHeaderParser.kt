import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket

object HttpHeaderParser {

    fun parseHeader(buffer: BufferedReader): HttpResult {
//        println("reading bytes")

        var lineOfString: String? = ""
        var response = ""
        while (lineOfString != null) {
            lineOfString = buffer.readLine()
//            println(lineOfString)
            response += lineOfString + "\n"

            if (lineOfString.isBlank()) {
                break
            }
        }
//        println("read header complete")

        val code = parseCode(response.substringBefore("\n"))
        val status = parseStatus(response.substringBefore("\n"))
        val contentType = parseContentType(response)


        val httpUrl = parseHttpUrl(response)

        return if (httpUrl != null) {
            val socket = Socket(httpUrl.host, 80)

            val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val bufferOut = BufferedOutputStream(socket.getOutputStream())

            println("redirecting to ${httpUrl.protocol}://${httpUrl.host}/${httpUrl.url}\n\n")
            bufferOut.write((
                "${httpUrl.method} /${httpUrl.url} HTTP/1.1\r\n" +
                "Host: ${httpUrl.host}\r\n\r\n"
            ).toByteArray())
            bufferOut.flush()

            val header = parseHeader(bufferedReader)
            socket.close()
            header
        } else {
            HttpResult(
                code = code,
                status = status,
                contentType = contentType,
                content = response)
        }
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


    private fun parseCode(header: String): Int {
        return header.split(" ")[1].toInt()
    }

    private fun parseContentType(header: String): String {
        return header
            .substringAfter("Content-Type: ", "")
            .substringBefore("\n", "")
    }

    private fun parseHttpUrl(header: String): HttpUrl? {
        var httpProtocol = ""
        var rawUrl = ""
        var redirectUrl = ""
        var redirectHost = ""
        val parseRawUrl = {
            httpProtocol = rawUrl.substringBefore(":")

            val temp = rawUrl.substringAfter("://", "")
            redirectHost = temp.substringBefore("/")
            redirectUrl = temp.substringAfter("/", "").substringBefore("\n")
        }

        if (header.substringAfter("Refresh: ", "").isNotBlank()) {
            rawUrl = header
                .substringAfter("Refresh: ")
                .substringBefore("\n", "")
        } else if (header.substringAfter("Location: ", "").isNotBlank()) {
            rawUrl = header
                .substringAfter("Location: ")
                .substringBefore("\n", "")
        }

        parseRawUrl()


        println("redirectProtocol $httpProtocol")
        println("redirectHost $redirectHost")
        println("redirectUrl $redirectUrl")

        return if (redirectHost.isNotBlank()) {
            HttpUrl(
                protocol = httpProtocol,
                host = redirectHost,
                url = redirectUrl,
                method = "GET"
            )
        } else {
            null
        }
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

            400 -> "The server cannot or will not process the request due to something that is perceived to be a client error (e.g., malformed request syntax, invalid request message framing, or deceptive request routing)."
            401 -> "Although the HTTP standard specifies \"unauthorized\", semantically this response means 'unauthenticated'. That is, the client must authenticate itself to get the requested response."
            402 -> "This response code is reserved for future use. The initial aim for creating this code was using it for digital payment systems, however this status code is used very rarely and no standard convention exists."
            403 -> "The client does not have access rights to the content; that is, it is unauthorized, so the server is refusing to give the requested resource. Unlike 401 Unauthorized, the client's identity is known to the server."
            404 -> "The server can not find the requested resource. In the browser, this means the URL is not recognized. In an API, this can also mean that the endpoint is valid but the resource itself does not exist. Servers may also send this response instead of 403 Forbidden to hide the existence of a resource from an unauthorized client. This response code is probably the most well known due to its frequent occurrence on the web."
            405 -> "The request method is known by the server but is not supported by the target resource. For example, an API may not allow calling DELETE to remove a resource."
            406 -> "This response is sent when the web server, after performing server-driven content negotiation, doesn't find any content that conforms to the criteria given by the user agent."
            407 -> "This is similar to 401 Unauthorized but authentication is needed to be done by a proxy."
            408 -> "This response is sent on an idle connection by some servers, even without any previous request by the client. It means that the server would like to shut down this unused connection."
            409 -> "This response is sent when a request conflicts with the current state of the server."
            410 -> "This response is sent when the requested content has been permanently deleted from server, with no forwarding address. Clients are expected to remove their caches and links to the resource. The HTTP specification intends this status code to be used for 'limited-time, promotional services'. APIs should not feel compelled to indicate resources that have been deleted with this status code."
            411 -> "Server rejected the request because the Content-Length header field is not defined and the server requires it."
            412 -> "The client has indicated preconditions in its headers which the server does not meet."
            413 -> "Request entity is larger than limits defined by server. The server might close the connection or return an Retry-After header field."
            414 -> "The URI requested by the client is longer than the server is willing to interpret."
            415 -> "The media format of the requested data is not supported by the server, so the server is rejecting the request."
            416 -> "The range specified by the Range header field in the request cannot be fulfilled. It's possible that the range is outside the size of the target URI's data."
            417 -> "This response code means the expectation indicated by the Expect request header field cannot be met by the server."
            418 -> "The server refuses the attempt to brew coffee with a teapot."
            421 -> "The request was directed at a server that is not able to produce a response. This can be sent by a server that is not configured to produce responses for the combination of scheme and authority that are included in the request URI."
            422 -> "The request was well-formed but was unable to be followed due to semantic errors."
            423 -> "The resource that is being accessed is locked."
            424 -> "The request failed due to failure of a previous request."
            425 -> "Indicates that the server is unwilling to risk processing a request that might be replayed."
            426 -> "The server refuses to perform the request using the current protocol but might be willing to do so after the client upgrades to a different protocol. The server sends an Upgrade header in a 426 response to indicate the required protocol(s)."
            428 -> "The origin server requires the request to be conditional. This response is intended to prevent the 'lost update' problem, where a client GETs a resource's state, modifies it and PUTs it back to the server, when meanwhile a third party has modified the state on the server, leading to a conflict."
            429 -> "The user has sent too many requests in a given amount of time ('rate limiting')."
            431 -> "The server is unwilling to process the request because its header fields are too large. The request may be resubmitted after reducing the size of the request header fields."
            451 -> "The user agent requested a resource that cannot legally be provided, such as a web page censored by a government."

            500 -> "The server has encountered a situation it does not know how to handle."
            501 -> "The request method is not supported by the server and cannot be handled. The only methods that servers are required to support (and therefore that must not return this code) are GET and HEAD."
            502 -> "This error response means that the server, while working as a gateway to get a response needed to handle the request, got an invalid response."
            503 -> "The server is not ready to handle the request. Common causes are a server that is down for maintenance or that is overloaded. Note that together with this response, a user-friendly page explaining the problem should be sent. This response should be used for temporary conditions and the Retry-After HTTP header should, if possible, contain the estimated time before the recovery of the service. The webmaster must also take care about the caching-related headers that are sent along with this response, as these temporary condition responses should usually not be cached."
            504 -> "This error response is given when the server is acting as a gateway and cannot get a response in time."
            505 -> "The HTTP version used in the request is not supported by the server."
            506 -> "The server has an internal configuration error: the chosen variant resource is configured to engage in transparent content negotiation itself, and is therefore not a proper end point in the negotiation process."
            507 -> "The method could not be performed on the resource because the server is unable to store the representation needed to successfully complete the request."
            508 -> "The server detected an infinite loop while processing the request."
            509 -> "Further extensions to the request are required for the server to fulfill it."
            511 -> "Indicates that the client needs to authenticate to gain network access."
            else -> ""
        }
    }

}