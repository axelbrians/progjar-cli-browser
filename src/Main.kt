import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.URL

fun main() {
    "https://pbs.twimg.com/media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    "http://monta.if.its.ac.id"
    "http://monkp.if.its.ac.id"
    val targetHost = "pbs.twimg.com"
    val targetUrl= "media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    val targetUri = "https://$targetHost/$targetUrl"

    val socket = Socket(targetHost, 80)

    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val bufferOut = BufferedOutputStream(socket.getOutputStream())

    println("asking for $targetUri")
    bufferOut.write((
            "GET /$targetUrl HTTP/1.1\r\n" +
            "Host: $targetHost\r\n\r\n"
            ).toByteArray())
    bufferOut.flush()

    val httpResult = HttpHeaderParser.parseHeader(bufferedReader)

    with(httpResult) {
        println("code: $code")
        println("status: $status")
        println("contentType: $contentType")
    }

    socket.close()
}

