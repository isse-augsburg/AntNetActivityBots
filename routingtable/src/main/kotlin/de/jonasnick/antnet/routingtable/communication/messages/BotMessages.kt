package de.jonasnick.antnet.routingtable.communication.messages


/**
 * General Interface for the messages between this and the bots
 * Messages send as a binary array to allow for more data compression
 */
interface BotMessage {
    /**
     * Converts the object into the bytearray
     * Is the reverse function of [MessageDecoder.decode]
     */
    fun toBytes(): ByteArray

    /**
     * Identifier of the message
     * Has to be the same as [MessageDecoder.msgID]
     */
    val msgID: Int
}

/**
 * Interface for decoding a byte array message back to the object
 */
interface MessageDecoder<T : BotMessage> {
    /**
     * Return null when there bytearray didn't match the parsing
     *
     * Is the reverse function of [BotMessage.toBytes]
     */
    fun decode(bytes: ByteArray): T?

    /**
     * Identifier of the message
     * Has to be the same as [BotMessage.msgID]
     */
    val msgID: Int
}

/**
 * Global singleton to hold all message types and help with en- and decoding
 */
object BotMessagesHandler {
    private val map = mutableMapOf<Int, MessageDecoder<*>>()

    /**
     * Registers a unique message with a given decoder
     */
    fun register(decoder: MessageDecoder<*>, id: Int = decoder.msgID) {
        require(!map.containsKey(id)) { "ID $id already registered, unregister first" }
        map[id] = decoder
    }

    /**
     * Removes the decoder for the given id
     */
    fun unregister(id: Int) = map.remove(id)

    /**
     * Decodes the ByteArray with the Decoder for the given ID
     */
    fun decode(bytes: ByteArray, id: Int): BotMessage? = map[id]?.decode(bytes)

    /**
     * Encodes a message (which could also be done directly with the Message Object)
     */
    fun encode(message: BotMessage): ByteArray = message.toBytes()
}