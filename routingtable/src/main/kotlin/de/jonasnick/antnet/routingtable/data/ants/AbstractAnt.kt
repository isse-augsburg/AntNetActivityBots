package de.jonasnick.antnet.routingtable.data.ants

import de.jonasnick.antnet.routingtable.data.AntConstants
import de.jonasnick.antnet.routingtable.data.map.Connection
import de.jonasnick.antnet.routingtable.data.map.NodeManager
import kotlin.math.pow

/**
 * The AbstractAnt is the code, that is the same for the virtual and real Ant
 * This is mostly the algorithmic stuff and the code to manipulate the virtual environment
 */
abstract class AbstractAnt(
    var startNode: Int,
    var targetNode: Int,
    protected val nodes: NodeManager
) : IAnt {
    companion object {
        var idCounter = 0
        const val PRINT_DEBUG = false
    }

    /** All the already visited nodes */
    protected val visited: MutableMap<Int, Int> = mutableMapOf()
    override var id: Int = idCounter++
    override var totalTimeTraveled: Double = 0.0
        protected set
    protected var state = AntState.MOVING
    var currentMoving = 0.0
        protected set
    var toMove = 0.0
        protected set
    override var toNode = startNode
        protected set
    override var fromNode = startNode
        protected set

    /** flag to keep the gui from changing position every time */
    override var nodeChanged = true


    /**
     * Selects the nodeID of the next Node to take with the Dogrio algorithm
     *
     * This uses the parameters set in the {AntConstants}
     */
    protected fun selectNext(): Map.Entry<Int, Connection>? {
        val notVisited = nodes.getConnections(toNode)
            .filter { !visited.contains(it.key) }

        if (notVisited.isEmpty())
            return null

        val sumView = notVisited.values.sumByDouble { it.view }
        val sumPheromones = notVisited.values.sumByDouble { it.pheromones }

        val list = mutableListOf<Pair<Double, Map.Entry<Int, Connection>>>()

        for (entry in notVisited) {
            val probFactor =
                (entry.value.pheromones / sumPheromones).pow(AntConstants.FACTOR_PHEROMONES) *
                        (entry.value.view / sumView).pow(AntConstants.FACTOR_DISTANCE)
            list += Pair(probFactor, entry)
        }

        return selectRandom(list)
    }

    /**
     * Selects a weighted element from the list
     * Double is the weight
     */
    protected fun <T> selectRandom(list: List<Pair<Double, T>>): T {
        var rand = Math.random() * list.sumByDouble { it.first }
        for (pair in list) {
            rand -= pair.first
            if (rand <= 0)
                return pair.second
        }

        return list.last().second
    }

    /**
     * Resets the Ant and send it back to the start of the graph
     */
    fun resetAnt(startNode: Int, targetNode: Int) {
        state = AntState.MOVING
        totalTimeTraveled = 0.0
        currentMoving = 0.0
        toMove = 0.0
        visited.clear()
        this.toNode = startNode
        this.startNode = startNode
        this.targetNode = targetNode
        nodeChanged = true
    }


    /**
     * Updates the pheromones along the path it has visited
     */
    fun updatePheromones() {
        for (entry in visited) {
            try {
                nodes.updateConnection(entry.key, entry.value) { it.copyWithAdjustedPheromones(totalTimeTraveled) }
            } catch (ex: IllegalArgumentException) {
                println("Updating pheromones to a already removed edge, ignoring")
            }
        }
    }

    /**
     * The progress along the current connection in percent
     * used to accurately show the position in the gui
     */
    override val progress: Double
        get() {
            if (toMove == 0.0)
                return 0.0
            return currentMoving / toMove
        }


    override fun toString(): String {
        return "Ant[$startNode->$targetNode: $toNode](visited=$visited, totalTimeTraveled=$totalTimeTraveled, state=$state)"
    }
}