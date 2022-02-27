package com.machina.downloader

import com.machina.HttpHeaderParser
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.DataInputStream
import java.io.File
import java.net.Socket

class FileDownloader {

    private val coroutineScope = CoroutineScope(Dispatchers.Default)
    private val jobs = mutableListOf<Job>()

    fun downloadFile(
        buffer: DataInputStream,
        fileName: String = System.currentTimeMillis().toString(),
        contentType: String = "",
        contentLength: Long = 0
    ): File {
        try {
            val fileExtension = getFileExtensionFromMimeType(contentType)
            val file = File(DOWNLOAD_DIR, "$fileName.$fileExtension")
            val fos = file.outputStream()

            val byteArraySize = 1024 * 10
            val byteArray = ByteArray(byteArraySize)
            var byteRead = 0L
            var readStatus = buffer.read(byteArray)

            println("starting download $fileName.$fileExtension")
            fos.use {
                while (readStatus != -1) {
                    val percentage = (byteRead.toDouble() / contentLength.toDouble()) * 100L
                    println("$fileName.$fileExtension $percentage%")
                    it.write(byteArray, 0, readStatus)
                    byteRead += readStatus
                    if (byteRead >= contentLength) {
                        if (contentLength > 0)
                            break
                    }

                    readStatus = buffer.read(byteArray)
                }
            }

            println("$fileName.$fileExtension downloaded")


            return file
        } catch (e: Exception) {
            return File(DOWNLOAD_DIR)
        }
    }

    fun downloadAll(queries: List<DownloadQuery>) {
        val index = 1

        val targetHostList = listOf(
            "dl-cdn.alpinelinux.org",
            "pbs.twimg.com",
        )

        val targetUrlList = listOf(
            "alpine/v3.15/releases/s390x/alpine-standard-3.15.0-s390x.iso",
            "media/FMkTSGgXEAUmxF7?format=jpg&name=4096x4096",
        )
        val sockets = mutableListOf<Socket>()
        val tempQueries = mutableListOf<DownloadQuery>()

        repeat(2) {
            val socket = Socket(targetHostList[index], 80)
//    var bufferedReader = BufferedReader(InputStreamReader(socket.getInputStream()))
            val dataInput = DataInputStream(socket.getInputStream())
            val bufferOut = BufferedOutputStream(socket.getOutputStream())
            val request = "GET /${targetUrlList[index]} HTTP/1.1\r\n" +
                    "Host: ${targetHostList[index]}\r\n" +
                    "User-Agent: KosimCLI/2.0\r\n" +
                    "Cache-Control: no-cache\r\n\r\n"

            bufferOut.write(request.toByteArray())
            bufferOut.flush()

            var lineOfString: String? = ""
            var response = ""
            var contentHeader = ""
            while (lineOfString != null) {
                lineOfString = dataInput.readLine()
                println(lineOfString)
                contentHeader += lineOfString + "\n"

                if (lineOfString.isBlank()) {
                    break
                }

            }

            val contentType = HttpHeaderParser.parseContentType(contentHeader)
            val contentLength = HttpHeaderParser.parseContentLength(contentHeader)
            tempQueries.add(DownloadQuery(
                dataInput, contentType = contentType, contentLength = contentLength
            ))
        }

        runBlocking {

            tempQueries.forEach { query ->
                println("job for $query")
                val job = coroutineScope.launch {
                    downloadFile(
                        query.buffer,
                        query.fileName,
                        query.contentType,
                        query.contentLength)
                }
                jobs.add(job)
            }

            jobs.forEach {
                it.join()
            }

            sockets.forEach { it.close() }
        }
    }

    private fun getFileExtensionFromMimeType(mimeType: String): String {
        return when (mimeType) {
            "image/jpeg" -> "jpg"
            "image/png" -> "png"
            "image/gif" -> "gif"
            "image/svg+xml" -> "svg"
            "image/webp" -> "webp"
            "video/webm" -> "webm"
            "video/x-msvideo" -> "avi"
            "video/mp4" -> "mp4"
            "video/mpeg" -> "mpeg"
            "video/ogg" -> "ogv"
            "video/mp2t" -> "ts"
            "video/3gpp" -> "3gp"
            "application/json" -> "json"
            "application/pdf" -> "pdf"
            "application/zip" -> "zip"
            "application/x-7z-compressed" -> "7z"
            "application/gzip" -> "gz"
            "application/vnd.rar" -> "rar"
            "application/octet-stream" -> "iso"
            "application/x-iso9660-image" -> "iso"
            else -> ""
        }
    }

    companion object {
        private const val DOWNLOAD_DIR = "C:\\Users\\Pegipegi\\Downloads"
    }

}