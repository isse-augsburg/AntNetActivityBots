package de.jonasnick.antnet.routingtable.communication

import de.jonasnick.antnet.routingtable.communication.PingState.*
import de.jonasnick.antnet.routingtable.communication.messages.*
import de.jonasnick.antnet.routingtable.data.AntManager
import de.jonasnick.antnet.routingtable.data.ants.RealAnt
import java.net.Socket
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.schedule
import kotlin.concurrent.thread

enum class PingState {
    PING_AWAITING,
    PING_RECEIVED,
    PING_TIMEOUT,
    PING_SLEEPING
}

/**
 * Class that manages the connection between the Server and each individual Ant
 * One AntConnector exists per Bot
 *
 * Handles the communication and reacting to requests and pings
 */
class AntConnector(private val ip: String, private val antManager: AntManager) {
    companion object {
        const val TIMEOUT = 2000L //ms
        const val PING_TIME = 5000L //ms
    }

    private var wifi: ClientHandler? = null
    private var killed: Boolean = false
    private var lastPing: AtomicLong = AtomicLong(0)
    private var pingSendTime = System.currentTimeMillis()
    var connected: AtomicBoolean = AtomicBoolean(false)
    var connecting: AtomicBoolean = AtomicBoolean(false)
    var inErrorState: AtomicBoolean = AtomicBoolean(false)
    var realAnt = RealAnt(
        antManager.startNode,
        antManager.targetNode,
        antManager.nodeManager,
        antManager
    )

    var state = PingState.PING_SLEEPING
        private set
    private var lastPingTimerTask: TimerTask? = null
    private var recurrentPingTask: TimerTask? = null

    private fun localPrintln(s: String) {
        println("[$ip]: $s")
    }

    /**
     * Pings every [PING_TIME]ms and waits for [TIMEOUT]ms until disconnecting and deleting this Ant
     */
    fun ping() {
        wifi?.let {
            if (state == PING_TIMEOUT) {
                localPrintln("already in timeout state, not pinging")
                return
            }


            pingSendTime = System.currentTimeMillis()
            it.write(byteArrayOf(1), 10)
            state = PING_AWAITING

            lastPingTimerTask = ConnectionThreadPool.timer.schedule(TIMEOUT) {
                localPrintln("Timeouted")
                lastPingTimerTask = null
                state = PING_TIMEOUT
                inErrorState.set(true)
                disconnect()
            }
        }
    }

    /**
     * Tries to connect the Ant in a different thread (to not hang the program on timeouts)
     *
     * Starts the ping task once connected
     * Adds a real ant to the [AntManager] and the wifi handler to the [ConnectionThreadPool]
     */
    fun connect() {
        connecting.set(true)
        thread {
            try {

                val socket = Socket(ip, 23)
                wifi =
                    ClientHandler(socket, this::receivedBotMessage, onError = { inErrorState.set(true); disconnect() })
                wifi?.let {
                    ConnectionThreadPool.addClientHandler(it)
                }
                synchronized(antManager.ants) {
                    if (!antManager.ants.contains(realAnt)) {
                        antManager.ants.add(realAnt)
                        localPrintln("Bot $ip connected")
                    }
                }

                connected.set(true)
                connecting.set(false)

                recurrentPingTask = ConnectionThreadPool.timer.schedule(1000, PING_TIME) {
                    ping()
                }
            } catch (e: Exception) {
                inErrorState.set(true)
            }
        }
    }

    /**
     * Disconnects the bot and cleans up its remains
     * - Deletes the real ant and removes the sprites
     * - Removes the wifi handler from the [ConnectionThreadPool]
     * - cancels the pings
     */
    fun disconnect() {
        synchronized(antManager.ants) {
            if (antManager.ants.contains(realAnt)) {
                antManager.ants.remove(realAnt)
                antManager.gui.gm.sm.removeSprite("${realAnt.id}")

                localPrintln("Bot $ip disconnected")
            }
        }

        wifi?.let {
            ConnectionThreadPool.removeClientHandler(it)
        }

        recurrentPingTask?.cancel()
        lastPingTimerTask?.cancel()

        connected.set(false)
    }

    fun kill() {
        killed = true
    }

    /**
     * Gets called once a message from the bot is received and decoded
     * decides what to do next with it
     */
    private fun receivedBotMessage(msg: BotMessage) {
        when (msg) {
            is RoundaboutRequestMSG -> receivedRoundaboutRequest(msg)
            is PingResponseMSG -> receivedPing()
            else -> localPrintln("Unkown Package arrived!")
        }
        /*
        when (msg.msgID) {
            1 -> wifi?.write(byteArrayOf(1), 2)
            11 -> receivedPing()
            else -> localPrintln("Unkown Package arrived!")
        }*/
    }

    /**
     * Checks on a ping response if it has timed out or if is still in the time frame
     */
    private fun receivedPing() {
        if (state == PING_AWAITING) {
            lastPing.set(System.currentTimeMillis())
            lastPingTimerTask?.cancel()

            val pingDuration = System.currentTimeMillis() - pingSendTime

            state = PING_RECEIVED
            // localPrintln("Got ping response after $pingDuration ms")
        } else {
            localPrintln("Got ping out of time")
        }

    }

    /**
     * When receiving a request for an exit is being delegated to [RealAnt.requestNextExit]
     */
    private fun receivedRoundaboutRequest(msg: RoundaboutRequestMSG) {
        localPrintln("request: $msg")
        if (!msg.isExit) {
            val res = realAnt.requestNextExit(msg.id)

            println("Roundabout ${msg.id} is choosing exit $res")
            wifi?.write(res)
        }
    }
}

/**
 * Main Function for testing the connections to the bots
 */
fun main() {
    // val prefix = "192.168.188."
    val prefix = "192.168.43."
    val connectedBots = mutableListOf<AntConnector>()
    BotMessagesHandler.register(RoundaboutRequestMSG, RoundaboutRequestMSG.msgID)
    BotMessagesHandler.register(RoundaboutResponseMSG, RoundaboutResponseMSG.msgID)
    BotMessagesHandler.register(PingRequestMSG, PingRequestMSG.msgID)
    BotMessagesHandler.register(PingResponseMSG, PingResponseMSG.msgID)

    val am = AntManager()

    val map = mutableMapOf<Int, AntConnector>()
    val lastPing = 0


    while (true) {
        for (i in 1..254) {
            var con = map[i]
            if (con == null || con.inErrorState.get()) {
                val connector = AntConnector(prefix + i, am)
                map[i] = connector
                con = connector

                connector.connect()
            }
            /*if (con.connected.get())
                con.ping()*/
        }

        Thread.sleep(2000)
    }

    //for (i in 1..254)
    //for (i in 170..180)
    //    AntConnector(prefix + i, connectedBots)
}

