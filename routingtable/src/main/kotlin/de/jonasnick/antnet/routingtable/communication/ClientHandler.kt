package de.jonasnick.antnet.routingtable.communication

import de.jonasnick.antnet.routingtable.communication.messages.BotMessage
import de.jonasnick.antnet.routingtable.communication.messages.BotMessagesHandler
import de.jonasnick.antnet.routingtable.communication.messages.RoundaboutResponseMSG
import java.io.BufferedInputStream
import java.io.BufferedOutputStream
import java.io.IOException
import java.net.ServerSocket
import java.net.Socket
import java.util.*
import java.util.concurrent.ConcurrentLinkedDeque
import kotlin.concurrent.thread
import kotlin.math.min


typealias ClientReceivedMsgCallback = (msg: BotMessage) -> Unit

/**
 *
 * Protkoll:
 *
 * Byte:
 * +---------+
 * |         |
 * +---------+
 *
 * Multiple Bytes:
 * +=========+
 * |         |
 * +=========+
 *
 *
 *
 * Frame:
 * +---------++---------++---------++---------++---------++---------++=========++---------+
 * |  0x6E   ||  XXXX   ||  XXXX   ||  XXXX   ||  XXXX   ||  YYYY   ||   MSG   ||  0x7F   |
 * +---------++---------++---------++---------++---------++---------++=========++---------+
 *
 * 0x6E and 0x7F are arbitrarily selected magic numbers
 *
 * XXXX-XXXX-XXXX-XXXX: Length of the message _without_ START and END byte and _without_ ID
 * YYYY: ID of the message
 *
 */
class ClientHandler(
    socket: Socket,
    private val callback: ClientReceivedMsgCallback? = null,
    private val debugLevel: Int = 1,
    val onError: (() -> Unit)? = null
) {
    companion object {
        const val END_BYTE: Byte = 0x7F
        const val START_BYTE: Byte = 0x6E
        const val BUFFER_SIZE: Int = 20000
        const val CHUNK_SIZE: Int = 1024
    }


    private var writer: BufferedOutputStream = BufferedOutputStream(socket.getOutputStream())
    private var reader: BufferedInputStream = BufferedInputStream(socket.getInputStream())
    private val queue: ConcurrentLinkedDeque<ByteArray> = ConcurrentLinkedDeque()
    private val buffer = ByteArray(BUFFER_SIZE + 7)

    private var length = 0
    private var typeID = 0
    private var bytesRead = 0
    private var startTime = System.nanoTime()

    fun tick() {
        try {
            // send actions
            sendQueue()
        } catch (e: Exception) {
            println("Error during sending: ${e.message}")
            // e.printStackTrace()
            onError?.invoke()
            throw e
        }

        try {
            // read actions
            if (length > 0) {
                // + 2 for end byte and start byte
                // + 1 for type
                // + 4 for size
                if ((length + 7) > bytesRead && reader.available() < min((length + 7) - bytesRead, CHUNK_SIZE))
                    return

                while ((length + 7) > bytesRead
                    && reader.available() >= min((length + 7) - bytesRead, CHUNK_SIZE)
                ) {
                    bytesRead += reader.read(
                        buffer, bytesRead, min(
                            (length + 7) - bytesRead, CHUNK_SIZE
                        )
                    )
                }

                if ((length + 7) > bytesRead)
                    return

                if (buffer[length + 6] == END_BYTE) { //Command valid

                    if (debugLevel >= 2)
                        println("Lenght: $length Type: $typeID fetched In: ${(System.nanoTime() - startTime) / 1000000.0}ms")
                    if (debugLevel >= 3) {
                        val joiner = StringJoiner(", ", "--> ", "")
                        for (i in buffer.indices) {
                            joiner.add(String.format("0X%02x", buffer[i]))
                        }
                        println(joiner.toString())
                    }

                    handleBuffer(length, typeID)

                } else { // didn't receive end byte
                    println("ERROR: Lenght: $length Type: $typeID")

                    if (debugLevel >= 3) {
                        val joiner = StringJoiner(", ", "ERROR: --> ", "")
                        for (i in buffer.indices) {
                            joiner.add(String.format("0X%02x", buffer[i]))
                        }

                        println(joiner.toString())
                    }

                    // flush (find better way?)
                    while (reader.read() != END_BYTE.toInt()) continue
                }

                length = -2
            } else {
                if (reader.available() >= 6) {
                    buffer[0] = reader.read().toByte()
                    if (buffer[0] == START_BYTE) {

                        buffer[1] = reader.read().toByte()
                        buffer[2] = reader.read().toByte()
                        buffer[3] = reader.read().toByte()
                        buffer[4] = reader.read().toByte()

                        buffer[5] = reader.read().toByte()

                        length = buffer.getInteger(1)
                        typeID = buffer[5].unsigned()
                        bytesRead = 6
                        startTime = System.nanoTime()

                        if (length > BUFFER_SIZE) {
                            println("Received object too large: $length")
                            length = -1
                        }
                    }
                }
            }
        } catch (e: Exception) {
            println("Error during reading")
            e.printStackTrace()
            onError?.invoke()
            throw e
        }
    }


    private fun handleBuffer(length: Int, typeID: Int) {
        val message = BotMessagesHandler.decode(buffer.sliceArray(6..(length + 5)), typeID)
        // println(message)
        if (callback != null && message != null) {
            callback.invoke(message)
        }
    }


    @Throws(IOException::class)
    private fun sendQueue() {
        while (queue.isNotEmpty()) {
            val arr = queue.poll() ?: return

            try {
                if (debugLevel >= 2)
                    println("Writing length ${arr.size}")
                if (debugLevel >= 2) {
                    val joiner = StringJoiner(", ", "writing: ", "")
                    for (i in arr.indices) {
                        joiner.add(String.format("0X%02x", arr[i]))
                    }
                    println(joiner.toString())
                }
                writer.write(arr)
                writer.flush()
            } catch (e: Exception) {
                queue.addFirst(arr)
                throw e
            }
        }
    }

    fun write(write: ByteArray, type: Int) {
        val size = write.size
        val arr = ByteArray(size + 7)
        write.copyInto(arr, 6)

        arr[0] = START_BYTE
        arr[size + 6] = END_BYTE
        arr.setInteger(1, size)
        arr[5] = type.toByte()

        queue.add(arr)
    }

    fun write(msg: BotMessage) {
        write(msg.toBytes(), msg.msgID)
    }
}

fun Byte.unsigned(): Int = java.lang.Byte.toUnsignedInt(this)

fun ByteArray.getInteger(index: Int): Int {
    return this[index].unsigned() shl 24 or (this[index + 1].unsigned() shl 16) or (this[index + 2].unsigned() shl 8) or (this[index + 3].unsigned())
}

fun ByteArray.getShort(index: Int): Int {
    return (this[index].unsigned() shl 8) or (this[index + 1].unsigned())
}

fun ByteArray.setInteger(index: Int, value: Int) {
    this[index] = (value shr 24 and 0xFF).toByte()
    this[index + 1] = (value shr 16 and 0xFF).toByte()
    this[index + 2] = (value shr 8 and 0xFF).toByte()
    this[index + 3] = (value and 0xFF).toByte()
}

fun ByteArray.setShort(index: Int, value: Int) {
    this[index] = (value shr 8 and 0xFF).toByte()
    this[index + 1] = (value and 0xFF).toByte()
}


fun main() {
    val server = ServerSocket(9999)
    println("Server is running on port ${server.localPort}")
    BotMessagesHandler.register(RoundaboutResponseMSG)

    while (true) {
        val client = server.accept()
        println("Client connected: ${client.inetAddress.hostAddress}")

        // Run client in it's own thread. (threads start in start function)
        val handler = ClientHandler(client)
        thread {
            while (true) handler.tick()
        }
        handler.write(byteArrayOf(123, 123, 123, 12, 31, 23, 123), 1)
    }
}