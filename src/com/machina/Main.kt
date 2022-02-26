package com.machina

import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.InputStreamReader
import java.net.Socket
import java.net.URLConnection
import java.util.*

fun main() {
    "http://monta.if.its.ac.id"
    "http://monkp.if.its.ac.id"
    "https://pbs.twimg.com/media/FMfbrkzaMAQJnaL?format=jpg&name=large"
    "http://basic.ichimarumaru.tech" + "kuncimenujulautan:tQKEJFbgNGC1NCZlWAOjhyCOm6o3xEbPkJhTciZN"
    "https://scontent-sin6-2.xx.fbcdn.net/v/t39.30808-6/274463149_1309864126191919_5646951478282062114_n.jpg?_nc_cat=102&ccb=1-5&_nc_sid=5cd70e&_nc_eui2=AeEjLu92sbRoU2a_GWOHvQITvDGcxCKP6oK8MZzEIo_qgiqAMFQ2piZGvaWCE7sPMEiuWSAzco2_nsDq7rpYExhi&_nc_ohc=XtIF7nUoC4sAX9jN5sd&_nc_ht=scontent-sin6-2.xx&oh=00_AT8hGfSv_NlTmHxgx4DjMBb2GTvXMat8jRgw9Dx4F9F6LA&oe=62200274"


    val sc = Scanner(System.`in`)
    val index = 1

    val targetHostList = listOf(
        "monkp.if.its.ac.id",
        "monta.if.its.ac.id",
        "pbs.twimg.com",
        "basic.ichimarumaru.tech",
        "scontent-sin6-2.xx.fbcdn.net"
    )

    val targetUrlList = listOf(
        "",
        "",
        "media/FMfbrkzaMAQJnaL?format=jpg&name=large",
        "",
        "v/t39.30808-6/274463149_1309864126191919_5646951478282062114_n.jpg?_nc_cat=102&ccb=1-5&_nc_sid=5cd70e&_nc_eui2=AeEjLu92sbRoU2a_GWOHvQITvDGcxCKP6oK8MZzEIo_qgiqAMFQ2piZGvaWCE7sPMEiuWSAzco2_nsDq7rpYExhi&_nc_ohc=XtIF7nUoC4sAX9jN5sd&_nc_ht=scontent-sin6-2.xx&oh=00_AT8hGfSv_NlTmHxgx4DjMBb2GTvXMat8jRgw9Dx4F9F6LA&oe=62200274"
    )

    var socket = Socket(targetHostList[index], 80)
//    var bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    var dataInput = DataInputStream(socket.getInputStream())
    var bufferOut = BufferedOutputStream(socket.getOutputStream())

    println("asking for https://${targetHostList[index]}/${targetUrlList[index]}")
    bufferOut.write((
            "GET /${targetUrlList[index]} HTTP/1.1\r\n" +
            "Host: ${targetHostList[index]}\r\n\r\n"
            ).toByteArray())
    bufferOut.flush()

//    var httpResult = HttpHeaderParser.parseHeader(bufferedReader)
    var httpResult = HttpHeaderParser.parseHeader(dataInput)

    socket.close()

    if(httpResult.basicAuth == 1) {
        socket = Socket(targetHostList[index], 80)
        dataInput = DataInputStream(socket.getInputStream())
//        bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
        bufferOut = BufferedOutputStream(socket.getOutputStream())
        print("Enter username: ")
        val username = sc.nextLine()
        print("Enter password: ")
        val password = sc.nextLine()
        val up = "$username:$password"
        val credential: String = Base64.getEncoder().encodeToString(up.toByteArray())
        bufferOut.write((
                "GET /${targetUrlList[3]} HTTP/1.1\r\n" +
                "Host: ${targetHostList[3]}\r\n" +
                "Authorization: Basic $credential\r\n\r\n"
                ).toByteArray())
        bufferOut.flush()

        httpResult = HttpHeaderParser.parseHeader(dataInput)
//        httpResult = HttpHeaderParser.parseHeader(bufferedReader)

        socket.close()
    }

    with(httpResult) {
        println("code: $code")
        println("status: $status")
        println("contentType: $contentType")
        println("contentLength $contentLength")

    }

}

