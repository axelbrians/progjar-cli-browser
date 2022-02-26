import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.Socket
import java.net.URL
import java.util.*

fun main() {
    "https://pbs.twimg.com/media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    "http://monta.if.its.ac.id"
    "http://monkp.if.its.ac.id"
    "http://basic.ichimarumaru.tech"
    "kuncimenujulautan:tQKEJFbgNGC1NCZlWAOjhyCOm6o3xEbPkJhTciZN"

    val sc = Scanner(System.`in`)

    val targetHostList = listOf(
        "monkp.if.its.ac.id",
        "monta.if.its.ac.id",
        "pbs.twimg.com",
        "basic.ichimarumaru.tech"
    )

    val targetUrlList = listOf(
        "",
        "",
        "media/FMfbrkzaMAQJnaL?format=jpg&name=large",
        ""
    )

    val socket = Socket(targetHostList[3], 80)

    val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    val bufferOut = BufferedOutputStream(socket.getOutputStream())

    println("asking for https://${targetHostList[3]}/${targetUrlList[3]}")
    bufferOut.write((
            "GET /${targetUrlList[3]} HTTP/1.1\r\n" +
            "Host: ${targetHostList[3]}\r\n\r\n"
            ).toByteArray())
    bufferOut.flush()

    var httpResult = HttpHeaderParser.parseHeader(bufferedReader)

    if(httpResult.basicAuth == 1) {
        val bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        val bufferOut = BufferedOutputStream(socket.getOutputStream())
        println("Enter username: ")
        val username = sc.nextLine()
        println("Enter password: ")
        val password = sc.nextLine()
        val up = "$username:$password"
        val credential: String = Base64.getEncoder().encodeToString(up.toByteArray())
        bufferOut.write((
                "GET /${targetUrlList[3]} HTTP/1.1\r\n" +
                "Host: ${targetHostList[3]}\r\n" +
                "Authorization: Basic $credential\r\n\r\n"
                ).toByteArray())
        bufferOut.flush()

        httpResult = HttpHeaderParser.parseHeader(bufferedReader)
    }

    with(httpResult) {
        println("code: $code")
        println("status: $status")
        println("contentType: $contentType")
    }

    socket.close()
}

