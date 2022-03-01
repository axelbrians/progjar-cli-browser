package com.machina

import com.machina.downloader.FileDownloader
import java.awt.Desktop
import java.io.BufferedOutputStream
import java.io.BufferedReader
import java.io.DataInputStream
import java.io.File
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

    while(true) {
        println("Axel CLI Web Browser. Type \"-1\" or \"exit\" to exit")
        print("URI: ")
        var input = sc.nextLine()

        if(input.equals("exit") || input.equals("-1")) {
            println("Exiting Axel CLI Web Browser")
            break;
        }

        val delim = "/"
        val httpRegex = "http.?://".toRegex()

        input = httpRegex.replace(input, "")
        val hostname = input.split(delim)[0]
        val pathLength = input.length - hostname.length - 1

        var path:String? = ""
        if(pathLength > 0) {
            path = input.takeLast(pathLength)
        }

        var socket = Socket(hostname, 80)
        var dataInput = DataInputStream(socket.getInputStream())
        var bufferOut = BufferedOutputStream(socket.getOutputStream())

//        println("hostname:$hostname")
//        println("path:$path")

        var request = "GET /${path} HTTP/1.1\r\n" +
                "Host: ${hostname}\r\n" +
                "User-Agent: KosimCLI/2.0\r\n" +
                "Cache-Control: no-cache\r\n\r\n"

//        println("asking for https://${input}")
//        println("with request \n$request")

        bufferOut.write(request.toByteArray())
        bufferOut.flush()

        var httpResult = HttpHeaderParser.parseHeader(
            host = hostname,
            fileUrl = path ?: "",
            buffer = dataInput)

        socket.close()

        if(httpResult.basicAuth == 1) {
            socket = Socket(hostname, 80)
            dataInput = DataInputStream(socket.getInputStream())
            bufferOut = BufferedOutputStream(socket.getOutputStream())
            print("Enter username: ")
            val username = sc.nextLine()
            print("Enter password: ")
            val password = sc.nextLine()
            val up = "$username:$password"
            val credential: String = Base64.getEncoder().encodeToString(up.toByteArray())
//            request =  "GET /${targetUrlList[3]} HTTP/1.1\r\n" +
//                    "Host: ${targetHostList[3]}\r\n" +
//                    "Authorization: Basic $credential\r\n\r\n"

            request = "GET /${path} HTTP/1.1\r\n" +
                    "Host: ${hostname}\r\n" +
                    "Authorization: Basic $credential\r\n" +
                    "User-Agent: KosimCLI/2.0\r\n" +
                    "Cache-Control: no-cache\r\n\r\n"
            bufferOut.write(request.toByteArray())
            bufferOut.flush()

            httpResult = HttpHeaderParser.parseHeader(
                host = hostname,
                fileUrl = path ?: "",
                buffer = dataInput)

            socket.close()
        }

        with(httpResult) {
//            println("Code: $code")
//            println("Status: $status")
//            println("Content Type: $contentType")
            when (content) {
                is File -> {
                    println("File detected, opening with default application. . .")
                    Desktop.getDesktop().open(content)
                }
                is HttpContent -> {
                    println(content.title)
                    println(content.text)
                }
                else -> {
                    println(content)
                }
            }

            println("- - - - - - - - - -\n")
        }



//    println("header:\n" + httpResult.contentHeader)
//    println("content:\n" + httpResult.content)
    }
}

