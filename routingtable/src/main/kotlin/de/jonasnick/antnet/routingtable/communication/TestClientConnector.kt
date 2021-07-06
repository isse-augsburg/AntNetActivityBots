package de.jonasnick.antnet.routingtable.communication

import java.io.BufferedOutputStream
import java.net.Socket
import java.util.*

fun main() {
    val socket = Socket("192.168.188.27", 23)
    // val socket = Socket("192.168.4.1", 23)

    val wifi = ClientHandler(socket)

    // wifi.start()

    // writer.writeOwn(byteArrayOf(1, 2, 3, 4, 5), 1)
    // writer.writeOwn(byteArrayOf(1, 2, 3, 4, 5), 2)
    // writer.writeOwn(byteArrayOf(1, 2, 3, 4, 5), 3)
    // writer.writeOwn(byteArrayOf(1, 2, 3, 4, 5), 4)


    for (i in 0..255) {
        wifi.write(byteArrayOf(1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5, 1, 2, 3, 4, 5), i)
        Thread.sleep(20)
    }

    /*writer.writeOwn(byteArrayOf(11, 12, 13, 14, 15), 98)

    writer.write(byteArrayOf(123, 123, 123))

    writer.writeOwn(byteArrayOf(11, 12, 13, 14, 15), 97)

    for (i in 0..1000) {
        writer.writeObject(TestClass("Lalala $i lolo", 2 * i))
    }*/

    Thread.sleep(10000)

}

fun BufferedOutputStream.writeOwn(write: ByteArray, type: Int) {
    val size = write.size
    val arr = ByteArray(size + 7)
    write.copyInto(arr, 6)

    arr[0] = ClientHandler.START_BYTE
    arr[size + 6] = ClientHandler.END_BYTE
    arr.setInteger(1, size)
    arr[5] = type.toByte()


    println("Array: ${Arrays.toString(arr)}")

    write(arr)
    flush()
}