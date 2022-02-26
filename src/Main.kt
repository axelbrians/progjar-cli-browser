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

    val targetHostList = listOf(
        "monkp.if.its.ac.id",
        "monta.if.its.ac.id",
        "pbs.twimg.com"
    )

    val targetUrlList = listOf(
        "",
        "",
        "media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    )

    val socket = Socket(targetHostList[0], 80)

    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val bufferOut = BufferedOutputStream(socket.getOutputStream())

    println("asking for https://${targetHostList[0]}/${targetUrlList[0]}")
    bufferOut.write((
            "GET /${targetUrlList[0]} HTTP/1.1\r\n" +
            "Host: ${targetHostList[0]}\r\n\r\n"
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

