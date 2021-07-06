package de.jonasnick.antnet.routingtable.communication

import java.util.*
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.concurrent.thread


/**
 * An global single to keep track of all [ClientHandler]s (and therefore the wifi connections)
 *
 * Uses only a single thread to tick all connections to save on system resources and not spawn a thread per bot
 */
object ConnectionThreadPool {
    private val streamList: ConcurrentLinkedQueue<ClientHandler> = ConcurrentLinkedQueue()
    private val killed = AtomicBoolean(false)
    val timer = Timer()

    init {
        thread {
            while (!killed.get()) {
                for (clientHandler in streamList) {
                    try {
                        clientHandler.tick()
                    } catch (e: Exception) {
                        removeClientHandler(clientHandler)
                    }
                }
            }
        }
    }

    fun addClientHandler(c: ClientHandler) {
        streamList.add(c)
    }

    fun removeClientHandler(c: ClientHandler) {
        streamList.remove(c)
    }

    fun kill() {
        killed.set(true)
    }
}