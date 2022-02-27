package com.machina.downloader

import java.io.DataInputStream

data class DownloadQuery(
    val buffer: DataInputStream,
    val fileName: String = System.currentTimeMillis().toString(),
    val contentType: String = "",
    val contentLength: Long = 0
)
