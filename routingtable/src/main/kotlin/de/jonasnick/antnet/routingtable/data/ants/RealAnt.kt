package de.jonasnick.antnet.routingtable.data.ants

import de.jonasnick.antnet.routingtable.communication.messages.RoundaboutResponseError
import de.jonasnick.antnet.routingtable.communication.messages.RoundaboutResponseMSG
import de.jonasnick.antnet.routingtable.data.AntFinish
import de.jonasnick.antnet.routingtable.data.AntManager
import de.jonasnick.antnet.routingtable.data.map.Maps
import de.jonasnick.antnet.routingtable.data.map.NodeManager
import kotlin.math.min

/**
 * The Real Ant that is connected via Wifi and the [de.jonasnick.antnet.routingtable.communication.ConnectionManager] to a real Ant written in c++
 * The connection is handled by an [de.jonasnick.antnet.routingtable.communication.AntConnector] that listens to messages from and to the Bot
 *
 * Gets created and destroyed once a connection gets created/lost
 */
class RealAnt(startNode: Int, targetNode: Int, nodes: NodeManager, val antManager: AntManager) :
    AbstractAnt(startNode, targetNode, nodes) {
    var lastRequest = 0
    var nextExitNumber = -1
    var firstNode = true
    var returningToBase = false

    /**
     * not really used, as it does not move by ticking
     *
     * even tho the correct position along the path can not be identified
     * it can be estimated by adding a small value onto the moving value to make it seem like it is moving in the gui
     */
    override fun tick(): AntState {
        if (currentMoving < toMove) {
            currentMoving = min(currentMoving + 0.1, toMove)
        }

        return AntState.MOVING
    }

    /**
     * Called from the AntConnector once the ant reaches a roundabout
     * Most logic for the Ant happens in here
     */
    fun requestNextExit(actualNode: Int): RoundaboutResponseMSG {
        // finds where the bot started
        if (firstNode) {
            firstNode = false

            if (startNode != actualNode) {
                return returnToBase(actualNode)
            } else {
                toNode = actualNode
            }
        }

        // checks if it is in an error state and is return to the base
        // stops returning once it reached the start point
        if (returningToBase && startNode == actualNode) {
            returningToBase = false
            fromNode = actualNode
            toNode = actualNode
        }

        // Was probably already requested for this node, so sending the same old result
        if (lastRequest == actualNode) {
            println("Was probably already requested for this node; Returning old value")
            return RoundaboutResponseMSG(nextExitNumber, RoundaboutResponseError.NONE)
        }

        lastRequest = actualNode

        // Something went wrong, as the target node was never reached
        if (actualNode != toNode) {
            println("Something went wrong! tonode not reached.")
            return returnToBase(actualNode)
        }

        // Reached the target node and updating the pheromones along the path
        if (actualNode == targetNode) {
            println("Ant $id done")
            state = AntState.FINISHED

            if (PRINT_DEBUG)
                println("Finished ($totalTimeTraveled) $this")

            updatePheromones()
            // finishes++
            // finishLength += ant.totalTimeTraveled

            // Adds the red dot into the scatter plot
            antManager.realAntFinishes += AntFinish(antManager.tick, totalTimeTraveled)

            println("Should turn around now")
            turnAntAround()
        }

        // antManager.realAntFinishes += AntFinish(antManager.tick, totalTimeTraveled)

        // checks where to go next
        val next = selectNext()
        // back to the start once reaching a dead end (or rather dead ant? :P)
        if (next == null) {
            if (PRINT_DEBUG)
                println("Dead end... ($toNode; $visited)")
            state = AntState.DEAD_END

            return returnToBase(actualNode)
        }

        // This only works if nothing has failed so far
        // drives to the next nodes and sends the correct exit number to the real bot
        visited[toNode] = next.key
        fromNode = toNode
        toNode = next.key
        totalTimeTraveled += next.value.distance
        currentMoving = 0.0
        toMove = next.value.distance
        nodeChanged = true
        nextExitNumber = next.value.exitNumberStart

        println("[$id] total distance: $totalTimeTraveled")

        return RoundaboutResponseMSG(nextExitNumber, RoundaboutResponseError.NONE)
    }

    /**
     * To avoid uselessly driving back without setting pheromones the ant turns its start and endpoint around
     * As the graph is bidirectional the same result is achieved
     */
    fun turnAntAround() {
        resetAnt(targetNode, startNode)
    }

    /**
     * Returns the ant to the base, as there are many failure that could have happened.
     * Most of them are unrecoverable, better to start fresh
     */
    fun returnToBase(actualNode: Int): RoundaboutResponseMSG {
        resetAnt(startNode, targetNode)
        returningToBase = true
        val exit = Maps.returnToBaseMap[actualNode] ?: 3 // 3 is the statistically best exit for this graph
        nextExitNumber = exit
        return RoundaboutResponseMSG(exit, RoundaboutResponseError.RETURNING_TO_BASE)
    }

    override fun toString(): String {
        return "Real" + super.toString()
    }
}