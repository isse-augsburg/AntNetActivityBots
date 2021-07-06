package de.jonasnick.antnet.routingtable

import de.jonasnick.antnet.routingtable.data.AntManager

/**
 * Main start function of the application
 *
 * Starts the AntManger in a loop which manages the connections and the simulation
 */
fun main() {
    val antManager = AntManager()
    antManager.runLoop()
}