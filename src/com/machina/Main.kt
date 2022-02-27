package com.machina

import com.machina.downloader.FileDownloader
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
    "https://pbs.twimg.com/media/FMkTSGgXEAUmxF7?format=jpg&name=4096x4096"
    "http://basic.ichimarumaru.tech" + "kuncimenujulautan:tQKEJFbgNGC1NCZlWAOjhyCOm6o3xEbPkJhTciZN"
    "https://cdn.discordapp.com/attachments/862018186685579334/947118411509538886/SPOILER_VP3.jpg"
    "https://releases.ubuntu.com/20.04.4/ubuntu-20.04.4-desktop-amd64.iso"
    "https://dl-cdn.alpinelinux.org/alpine/v3.15/releases/x86_64/alpine-standard-3.15.0-x86_64.iso"

    val sc = Scanner(System.`in`)
    val index = 5

    val targetHostList = listOf(
        "monkp.if.its.ac.id",
        "monta.if.its.ac.id",
        "pbs.twimg.com",
        "basic.ichimarumaru.tech",
        "cdn.discordapp.com",
        "releases.ubuntu.com",
    )

    val targetUrlList = listOf(
        "",
        "",
        "media/FMkTSGgXEAUmxF7?format=jpg&name=4096x4096",
        "",
        "attachments/862018186685579334/947118411509538886/SPOILER_VP3.jpg",
        "20.04.4/ubuntu-20.04.4-desktop-amd64.iso"
    )

    // To test parallel download with FileDownlaoder
    val downloader = FileDownloader()
    downloader.downloadAll(listOf())

    return Unit
    // Comment until here for normal behaviour

    var socket = Socket(targetHostList[index], 80)
//    var bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
    var dataInput = DataInputStream(socket.getInputStream())
    var bufferOut = BufferedOutputStream(socket.getOutputStream())
    var request = "GET /${targetUrlList[index]} HTTP/1.1\r\n" +
            "Host: ${targetHostList[index]}\r\n" +
            "User-Agent: KosimCLI/2.0\r\n" +
            "Cache-Control: no-cache\r\n\r\n"

    println("asking for https://${targetHostList[index]}/${targetUrlList[index]}")
    println("with request \n$request")
    bufferOut.write(request.toByteArray())
    bufferOut.flush()

//    var httpResult = HttpHeaderParser.parseHeader(bufferedReader)
    var httpResult = HttpHeaderParser.parseHeader(dataInput)

    socket.close()

    if(httpResult.basicAuth == 1) {
        socket = Socket(targetHostList[index], 80)
        dataInput = DataInputStream(socket.getInputStream())
        bufferOut = BufferedOutputStream(socket.getOutputStream())
        print("Enter username: ")
        val username = sc.nextLine()
        print("Enter password: ")
        val password = sc.nextLine()
        val up = "$username:$password"
        val credential: String = Base64.getEncoder().encodeToString(up.toByteArray())
        request =  "GET /${targetUrlList[3]} HTTP/1.1\r\n" +
                "Host: ${targetHostList[3]}\r\n" +
                "Authorization: Basic $credential\r\n\r\n"
        bufferOut.write(request.toByteArray())
        bufferOut.flush()

        httpResult = HttpHeaderParser.parseHeader(dataInput)
//        httpResult = HttpHeaderParser.parseHeader(bufferedReader

        socket.close()
    }

//    println("header:\n" + httpResult.contentHeader)
//    println("content:\n" + httpResult.content)

    with(httpResult) {
        println("code: $code")
        println("status: $status")
        println("contentType: $contentType")
        println("contentLength $contentLength")
        println("content $content")

    }

}

