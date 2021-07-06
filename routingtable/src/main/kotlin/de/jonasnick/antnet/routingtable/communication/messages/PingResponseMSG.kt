package de.jonasnick.antnet.routingtable.communication.messages

import de.jonasnick.antnet.routingtable.communication.unsigned

/**
 * Empty message that is being send back from the bot on a ping request
 */
data class PingResponseMSG(val pingtime: Int) :
    BotMessage {
    override val msgID: Int = Companion.msgID

    override fun toBytes(): ByteArray {
        return byteArrayOf(0)
    }

    companion object :
        MessageDecoder<PingResponseMSG> {
        override val msgID: Int = 11
        val SIZE = 1

        override fun decode(bytes: ByteArray): PingResponseMSG? {
            if (bytes.size != SIZE) return null

            return PingResponseMSG(
                bytes[0].unsigned()
            )
        }
    }
}