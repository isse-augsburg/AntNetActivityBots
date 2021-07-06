package de.jonasnick.antnet.routingtable.communication.messages

import de.jonasnick.antnet.routingtable.communication.unsigned


/**
 * Types of Errors that could occur
 */
enum class RoundaboutResponseError(val code: Byte) {
    NONE(0),
    REACHED_WRONG_NODE(1),
    WRONG_START(2),
    DEAD_END(3),
    RETURNING_TO_BASE(0),
    ;

    companion object {
        fun fromCode(code: Byte) = when (code) {
            0.toByte() -> NONE
            1.toByte() -> REACHED_WRONG_NODE
            2.toByte() -> WRONG_START
            3.toByte() -> DEAD_END
            else -> throw IllegalArgumentException("Unknown code $code")
        }
    }
}

/**
 * This message is the response to the [RoundaboutRequestMSG] and contains the exit to use
 * Also sends whether it has errored or not (to allow for special handling like stopping)
 *
 * Data:
 * +--------++--------+
 * |00000ZZZ||  ERROR |
 * +--------++--------+
 *
 * ZZ: Exit Numb
 * ERROR: Error code
 * 0 -> no error
 * 1 -> reached wrong exit
 */
data class RoundaboutResponseMSG(val exitNum: Int, val errorCode: RoundaboutResponseError) :
    BotMessage {
    override val msgID: Int = Companion.msgID

    override fun toBytes(): ByteArray {
        return byteArrayOf(exitNum.toByte(), errorCode.code)
    }

    companion object :
        MessageDecoder<RoundaboutResponseMSG> {
        override val msgID: Int = 2
        val SIZE = 2

        override fun decode(bytes: ByteArray): RoundaboutResponseMSG? {
            if (bytes.size != SIZE) return null

            return RoundaboutResponseMSG(
                bytes[0].unsigned(), RoundaboutResponseError.fromCode(bytes[1])
            )
        }
    }
}