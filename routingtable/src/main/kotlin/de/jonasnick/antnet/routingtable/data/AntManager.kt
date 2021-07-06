package de.jonasnick.antnet.routingtable.data

import de.jonasnick.antnet.routingtable.communication.ConnectionManager
import de.jonasnick.antnet.routingtable.data.ants.AntState
import de.jonasnick.antnet.routingtable.data.ants.IAnt
import de.jonasnick.antnet.routingtable.data.ants.VirtualAnt
import de.jonasnick.antnet.routingtable.data.map.Maps
import de.jonasnick.antnet.routingtable.data.map.NodeManager
import de.jonasnick.antnet.routingtable.gui.GuiSwing
import kotlin.concurrent.thread
import kotlin.math.max

data class AntFinish(val iteration: Int, val length: Double)

/**
 * Main class that combines everything
 *
 * Keeps track of all ants and the data of them.
 */
class AntManager {
    companion object {
        /**
         * Over what time frame the data in the line chart should be averaged
         */
        const val AVERAGE_TICKS = 100
    }

    var tick = 0

    val gui: GuiSwing
    val startNode = 1
    val targetNode = 6

    val nodeManager = NodeManager()
    val ants: MutableList<IAnt> = mutableListOf()
    val antFinishesXAvg: MutableList<Int> = mutableListOf()
    val antFinishesYAvg: MutableList<Double> = mutableListOf()

    val realAntFinishesXAvg: MutableList<Int> = mutableListOf()
    val realAntFinishesYAvg: MutableList<Double> = mutableListOf()


    val antFinishes: MutableList<AntFinish> = mutableListOf()
    val realAntFinishes: MutableList<AntFinish> = mutableListOf()

    var finishTickCounter = 0
    var finishes = 0
    var finishLength = 0.0

    /**
     * Inits the Manager with the correct settings given in [AntConstants] and the constructor
     */
    init {
        Maps.addShowcaseMap(this)
        this.nodeManager.updateAllConnections { it.copy(distance = it.distance * 10) }

        repeat(AntConstants.ANT_COUNT) {
            this.addAnt(startNode, targetNode)
        }

        gui = GuiSwing.createAndShowGUI(this)
    }

    /**
     * adds a new Virtual Ant with the given start and end node
     */
    fun addAnt(start: Int, end: Int) {
        synchronized(ants) {
            ants.add(VirtualAnt(start, end, nodeManager))
        }
    }

    /**
     * Has to be called every tick
     *
     * Ticks all of the ants which moves them one Length Unit
     * When a ant is finished it gets notes in the Chart
     *
     * Also evaporates the pheromones every tick
     */
    fun tick(index: Int) {
        tick = index
        synchronized(ants) {
            for (ant in ants) {
                if (ant.tick() == AntState.FINISHED) {
                    finishes++
                    finishLength += ant.totalTimeTraveled

                    antFinishes += AntFinish(index, ant.totalTimeTraveled)
                }
            }
        }

        if (finishTickCounter + AVERAGE_TICKS <= index) {
            if (finishes > 0) {
                antFinishesXAvg.add(finishTickCounter)
                antFinishesYAvg.add(finishLength / finishes)
            }
            finishTickCounter += AVERAGE_TICKS
            finishes = 0
            finishLength = 0.0
        }

        nodeManager.updateAllConnections {
            it.copy(
                pheromones = max(
                    AntConstants.PHEROMONE_BASE_CONST,
                    it.pheromones * (1 - AntConstants.FACTOR_EVAPORATION)
                )
            )

            // experimental evaporation
            // - 1/(0.01x + 1) + 1
            // it.copy(pheromones = max(AntConstants.PHEROMONE_BASE_CONST, - 1/(0.000001 * it.pheromones + 1) + 1))
        }
    }

    /**
     * Helper function to print all Ants
     */
    fun printAnts() {
        println("--------------------------------------")
        println("${ants.size} Ants are roaming around:")
        synchronized(ants) {
            for (ant in ants) {
                println(ant)
            }
        }
        println("--------------------------------------")
    }

    /**
     * Helper function to print all Connection of the graph
     */
    fun printMap() {
        println("######################################")
        nodeManager.printAllConnections()
        println("######################################")
    }

    /**
     * Start function of the AntManger and the Application
     *
     * Ticks the AntManger in the configured time interval (set in the gui)
     * Also starts a thread for the Connection Manager
     */
    fun runLoop() {
        thread {
            ConnectionManager(this).runLoop()
        }

        var iteration = 0
        var lastTime = System.currentTimeMillis()
        while (true) {
            iteration++
            tick(iteration)

            // Only updates the gui every 50ms (20 fps), prevents a ton of updates on fast update rates
            if (System.currentTimeMillis() - lastTime >= 50) {
                lastTime = System.currentTimeMillis()
                gui.gm.updateGraph()
                gui.chart.update()
            }

            Thread.sleep(gui.sleepTimeMillis, gui.sleepTimeNanos)
        }
    }
}