package de.jonasnick.antnet.routingtable.data.ants

import de.jonasnick.antnet.routingtable.data.map.NodeManager


/**
 * The Simulated Ant, ticked and controlled by the AntManager
 */
@Suppress("ConstantConditionIf")
class VirtualAnt(startNode: Int, targetNode: Int, nodes: NodeManager) : AbstractAnt(startNode, targetNode, nodes) {
    /**
     * Gets called every tick
     * Manages everything the ant has to do
     *
     * @return Returns the current state it is in
     */
    override fun tick(): AntState {
        // if it is in dead end state or finished it will return to the base next iteration
        if (state != AntState.MOVING) {
            resetAnt(startNode, targetNode)
        }

        // if it did reach its target yet it will just move
        if (toMove - currentMoving > 0.0) {
            currentMoving++
            return state
        }

        // checks whether it has reached the target and updates the pheromones along its path
        if (toNode == targetNode) {
            state = AntState.FINISHED

            if (PRINT_DEBUG)
                println("Finished ($totalTimeTraveled)   $this")


            updatePheromones()
            return AntState.FINISHED
        }

        // checks where to go next
        val next = selectNext()
        // if it has nowhere to go it is in a dead end as it already visited every node around it
        if (next == null) {
            if (PRINT_DEBUG)
                println("Dead end... ($toNode; $visited)")
            state = AntState.DEAD_END
            return AntState.DEAD_END
        }

        // reaching this means it has reached a node and has to be send to the next one
        // saves current travel distance and prepares for next move
        visited[toNode] = next.key
        fromNode = toNode
        toNode = next.key
        totalTimeTraveled += next.value.distance
        currentMoving = 0.0
        toMove = next.value.distance
        nodeChanged = true

        return AntState.MOVING
    }

    override fun toString(): String {
        return "Virtual" + super.toString()
    }
}