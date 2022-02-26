import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.net.Socket
import java.util.concurrent.ExecutorService

fun main() {
    val socket = Socket("monta.if.its.ac.id", 80)

    val bufferIn = BufferedInputStream(socket.getInputStream())
    val bufferOut = BufferedOutputStream(socket.getOutputStream())

    println("gonna  write to buferOut")
    bufferOut.write((
            "GET / HTTP/1.1\r\n" +
            "Host: monta.if.its.ac.id\r\n\r\n"
            ).toByteArray())
    bufferOut.flush()

    println("waiting to readAllBytes")

    var response = ""
    var byteArray = bufferIn.readNBytes(100)

    while (byteArray.isNotEmpty()) {
        response += String(byteArray)
        byteArray = bufferIn.readNBytes(100)
    }
    println(response)
    println("read buffer complete")
    socket.close()
}