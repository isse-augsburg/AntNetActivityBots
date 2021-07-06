package de.jonasnick.antnet.routingtable.communication.messages

/**
 * Every so often this empty message gets send to the Bot to check if it still alive
 */
class PingRequestMSG :
    BotMessage {
    override val msgID: Int = Companion.msgID

    override fun toBytes(): ByteArray {
        return byteArrayOf(0)
    }

    companion object :
        MessageDecoder<PingRequestMSG> {
        override val msgID: Int = 10
        val SIZE = 1

        override fun decode(bytes: ByteArray): PingRequestMSG? {
            return PingRequestMSG()
        }
    }
}