package de.jonasnick.antnet.routingtable.data.map

import de.jonasnick.antnet.routingtable.data.AntConstants
import java.beans.PropertyChangeEvent
import java.beans.PropertyChangeSupport


/**
 * Manages the all nodes and connections between them
 * This manager does not know anything of the split nodes, only the actual graph
 */
class NodeManager {
    /**
     * a map of all connections mapped by both nodes it is connected with
     */
    private var nodes: MutableMap<Int, MutableMap<Int, Connection>> = mutableMapOf()

    /**
     * To support change events
     */
    private var support = PropertyChangeSupport(this)

    /**
     * Adds a new connection or overwrites a existing one
     */
    fun addConnection(node1: Int, node2: Int, con: Connection) {
        val innerMap1 = nodes.getOrPut(node1, { mutableMapOf() })
        innerMap1[node2] = con

        val innerMap2 = nodes.getOrPut(node2, { mutableMapOf() })
        innerMap2[node1] = con.flipExitNums()

        support.firePropertyChange("add", null, Pair(node1, node2))
    }

    fun addConnection(node1: Int, node2: Int, dist: Double, exitNumNode1: Int, exitNumNode2: Int) {
        addConnection(
            node1, node2,
            Connection(
                AntConstants.DEFAULT_PHEROMONES,
                dist,
                exitNumNode1,
                exitNumNode2
            )
        )
    }

    /**
     * gets a Connection if it exists
     */
    fun getConnection(node1: Int, node2: Int): Connection? = nodes[node1]?.get(node2)

    /**
     * Gets all connections for a given node
     */
    fun getConnections(node: Int): Map<Int, Connection> = nodes[node] ?: emptyMap()

    /**
     * Deletes a connection from two given nodes
     * might leave a dangling empty list
     */
    fun deleteConnection(node1: Int, node2: Int) {
        nodes[node1]?.remove(node2)
        nodes[node2]?.remove(node1)

        support.firePropertyChange("delete", Pair(node1, node2), null)
    }


    /**
     * inline update Connection
     */
    fun updateConnection(node1: Int, node2: Int, conUpdate: (Connection) -> Connection) {
        val innerMap1 = nodes.getOrPut(node1, { mutableMapOf() })
        val connection = innerMap1[node2] ?: throw IllegalArgumentException("Trying to update a non existing node")
        val updatedCon = conUpdate(connection)

        innerMap1[node2] = updatedCon

        val innerMap2 = nodes.getOrPut(node2, { mutableMapOf() })
        innerMap2[node1] = updatedCon.flipExitNums()
    }

    /**
     * Run a function on all Nodes
     */
    fun updateAllConnections(conUpdate: (Connection) -> Connection) {
        for (innerMaps in nodes.values) {
            innerMaps.replaceAll { _: Int, u: Connection -> conUpdate(u) }
        }
    }

    /**
     * Runs an operation on every connection once, meaning it only in one direction not both
     */
    fun foreachConnectionUnique(op: (node1: Int, node2: Int, con: Connection) -> Unit) {
        val set = mutableSetOf<Pair<Int, Int>>()
        for (innerMaps in nodes) {
            for (value in innerMaps.value) {
                val from = innerMaps.key
                val to = value.key
                if (!set.contains(Pair(to, from))) {
                    set.add(Pair(from, to))
                    op(from, to, value.value)
                }
            }
        }
    }

    /**
     * Runs an operation on every node
     */
    fun foreachNode(op: (node: Int) -> Unit) = nodes.keys.forEach(op)

    /**
     * Helper function to print all connections
     */
    fun printAllConnections() {
        val set = mutableSetOf<Pair<Int, Int>>()
        for (innerMaps in nodes) {
            for (value in innerMaps.value) {
                val from = innerMaps.key
                val to = value.key
                if (!set.contains(Pair(to, from))) {
                    set.add(Pair(from, to))
                    println("[${from}->${to}]: ${value.value}")
                }
            }
        }
    }

    fun addChangeListener(listener: (PropertyChangeEvent) -> Unit) = support.addPropertyChangeListener(listener)
}
