package com.machina.downloader

import com.machina.HttpHeaderParser
import kotlinx.coroutines.*
import java.io.BufferedOutputStream
import java.io.ByteArrayOutputStream
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
            val downloadDir = File(HOME_DIR, "Downloads").also {
                if (!it.exists()) {
                    it.mkdir()
                }
            }
            val file = File(downloadDir, "$fileName.$fileExtension")
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
            return File(HOME_DIR)
        }
    }

    fun downloadFileInParallel(
        host: String = "",
        fileUrl: String = "",
        fileName: String = System.currentTimeMillis().toString(),
        contentType: String = "",
        contentLength: Long = 0
    ): File {
//    Range: bytes=200-1000, 2000-6576, 19000-
        var realSize = 0L
        val tempThread = 5L
        val segment = contentLength / tempThread
        val numberOfThreads = if (contentLength % tempThread != 0L) {
            tempThread + 1
        } else tempThread

        val coroutineScope = CoroutineScope(Dispatchers.Default)
        val jobs = mutableListOf<Job>()
        val fileExtension = FileDownloader().getFileExtensionFromMimeType(contentType)
        val downloadDir = File(HOME_DIR, "Downloads").also {
            if (!it.exists()) {
                it.mkdir()
            }
        }
        val file = File(downloadDir, "$fileName.$fileExtension").also {
            if (!it.exists()) {
                it.createNewFile()
            }
        }
        val fos = file.outputStream()
        val baosList = MutableList(numberOfThreads.toInt()) {
            ByteArrayOutputStream(0)
        }

        runBlocking {
            repeat(numberOfThreads.toInt()) { index ->
                val job = coroutineScope.launch {
                    val start: Long
                    val end: Long

                    if (index == tempThread.toInt()) {
                        start = segment * index
                        end = contentLength - 1
                    } else {
                        start = segment * index
                        end = segment * index + segment - 1
                    }

//                    println("thread $index range=$start-$end")

                    val outputBufferSize = (end - start + 1L).toInt()
                    baosList[index].close()
                    baosList[index] = ByteArrayOutputStream(outputBufferSize)

                    val socket = Socket(host, 80)
                    val dataInput = DataInputStream(socket.getInputStream())
                    val bufferOut = BufferedOutputStream(socket.getOutputStream())
                    withContext(Dispatchers.IO) {
                        val request = "GET /$fileUrl HTTP/1.1\r\n" +
                                "Host: $host\r\n" +
                                "User-Agent: KosimCLI/2.0\r\n" +
                                "Cache-Control: no-cache\r\n" +
                                "Range: bytes=$start-$end\r\n\r\n"

                        bufferOut.write(request.toByteArray())
                        bufferOut.flush()

                        var lineOfString: String? = ""
                        while (lineOfString != null) {
                            lineOfString = dataInput.readLine()
                            if (lineOfString.isBlank()) {
                                break
                            }
                        }
                    }

                    val byteArraySize = 1024 * 10
                    val byteArray = ByteArray(byteArraySize)
                    var byteRead = 0L
                    var readStatus = dataInput.read(byteArray)

//                    println("starting download $fileName.$fileExtension at thread $index")
                    while (readStatus != -1) {
//                        println("thread $index: $byteRead / $outputBufferSize")
                        synchronized(baosList) {
                            baosList[index].write(byteArray, 0, readStatus)
                        }
                        byteRead += readStatus
                        if (byteRead >= outputBufferSize) {
                            if (contentLength > 0)
                                break
                        }

                        readStatus = dataInput.read(byteArray)
                    }
                    realSize += byteRead
                    socket.close()
                }
                jobs.add(index, job)
            }

            jobs.forEachIndexed { index, job ->
//                println("waiting for thread $index")
                job.join()
                synchronized(fos) {
//                    println("thread $index write ${baosList[index].size()}")
                    baosList[index].writeTo(fos)
                    baosList[index].close()
                    baosList[index].reset()
                    baosList[index] = ByteArrayOutputStream(0)
//                baosList.removeAt(index)
                }
            }
        }
//    println("real byte read: ${realSize / 1000}KB")

        fos.close()
        return file
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

    fun getFileExtensionFromMimeType(mimeType: String): String {
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
        private val HOME_DIR = System.getProperty("user.home", "C:")
    }

}