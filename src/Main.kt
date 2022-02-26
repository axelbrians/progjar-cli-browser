import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.URL

fun main() {
//    val socket = Socket("monta.if.its.ac.id", 80)
    val socket = Socket("pbs.twimg.com", 80)

    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
//    val bufferIn = BufferedInputStream(socket.getInputStream())
    val bufferOut = BufferedOutputStream(socket.getOutputStream())

    val targetHost = "pbs.twimg.com"
    val targetUri = "media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    val targetUrl = "https://pbs.twimg.com/media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    "https://twitter.com/yuqipictures/status/1497406948266110978/photo/1"
    println("asking for $targetUrl")
    bufferOut.write((
            "GET /$targetUri HTTP/1.1\r\n" +
            "Host: $targetHost\r\n\r\n"
            ).toByteArray())
    bufferOut.flush()

    val httpResult = HttpHeaderParser.parseHeader(bufferedReader)

    with(httpResult) {
        println("code: $code")
        println("status: $status")
        println("contentType: $contentType")
    }
    println(httpResult.code)
    println(httpResult.status)
    println(httpResult.contentType)

    socket.close()
}

