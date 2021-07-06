package de.jonasnick.antnet.routingtable.communication

import de.jonasnick.antnet.routingtable.communication.messages.*
import de.jonasnick.antnet.routingtable.data.AntManager

/**
 * The ConnectionManager keeps track of new Bots that connect
 * Every 2 seconds it scans the IP-Space given in [prefix] for new bots which respond to the connect
 *
 * It has to be noted that the [prefix] depends on the network and the router being used.
 * Also the bots have to be set to AP-Mode and connect to the given network
 */
class ConnectionManager(private val antManager: AntManager) {
    //private val prefix = "192.168.137."
    /**
     * Base Mask of the IP Space -> has to be changed depending on the Network
     */
    private val prefix = "192.168.43."
    private val map = mutableMapOf<Int, AntConnector>()


    init {
        BotMessagesHandler.register(RoundaboutRequestMSG, RoundaboutRequestMSG.msgID)
        BotMessagesHandler.register(RoundaboutResponseMSG, RoundaboutResponseMSG.msgID)
        BotMessagesHandler.register(PingRequestMSG, PingRequestMSG.msgID)
        BotMessagesHandler.register(PingResponseMSG, PingResponseMSG.msgID)
    }

    fun runLoop() {
        while (true) {
            for (i in 1..254) {
                val con = map[i]
                if (con == null || (!con.connecting.get() && !con.connected.get()) || con.inErrorState.get()) {
                    val connector = AntConnector(prefix + i, antManager)
                    map[i] = connector
                    connector.connect()
                }
            }

            Thread.sleep(2000)
        }
    }
}

fun main() {
    val am = AntManager()

    ConnectionManager(am).runLoop()
}