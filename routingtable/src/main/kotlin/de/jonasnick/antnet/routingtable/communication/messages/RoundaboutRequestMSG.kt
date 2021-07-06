package de.jonasnick.antnet.routingtable.communication.messages

/**
 * Message that is getting send from the Bot to the Server on entering a roundabout
 *
 * Data:
 * +--------+
 * |00XXXyyZ|
 * +--------+
 *
 * XXX: ID of the roundabout/Node
 * yy: ID of the exit of the roundabout that has been used
 * Z: binary Flag whether it is an exit or an entry to the roundabout
 */
data class RoundaboutRequestMSG(val id: Int, val nr: Int, val isExit: Boolean) :
    BotMessage {
    override val msgID: Int = Companion.msgID

    @Deprecated("never used from Server to Bot")
    override fun toBytes(): ByteArray {
        throw NotImplementedError("never used from Server to Bot")
    }

    companion object :
        MessageDecoder<RoundaboutRequestMSG> {
        override val msgID: Int = 1
        val SIZE = 1

        override fun decode(bytes: ByteArray): RoundaboutRequestMSG? {
            if (bytes.size != SIZE) return null
            //println(bytes.contentToString())

            val id = bytes[0].toInt() shr 3
            val nr = (bytes[0].toInt() shr 1) and 0b11
            val isExit = (bytes[0].toInt() and 1) == 0


            return RoundaboutRequestMSG(
                id, nr, isExit
            )
        }
    }
}