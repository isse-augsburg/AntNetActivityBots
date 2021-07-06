package de.jonasnick.antnet.routingtable.data.map

import de.jonasnick.antnet.routingtable.data.AntConstants
import de.jonasnick.antnet.routingtable.data.AntConstants.ESTIMATED_SHORTEST_LENGTH
import de.jonasnick.antnet.routingtable.data.AntConstants.FACTOR_ADDING_PHEROMONES
import de.jonasnick.antnet.routingtable.data.AntConstants.FACTOR_ADDING_PHEROMONES_EXP
import kotlin.math.max
import kotlin.math.pow

/**
 * Data class for holding the information about the connections between two nodes
 * connections are bidirectional
 */
data class Connection(val pheromones: Double, val distance: Double, val exitNumberStart: Int, val exitNumberEnd: Int) {
    val view: Double get() = 1.0 / distance

    /**
     * This is an essential part of the ACO Algorithm
     * This calculates the pheromones that are getting added when an ant finishes the path
     */
    fun copyWithAdjustedPheromones(total_length: Double): Connection =
        this.copy(
            pheromones = max(
                AntConstants.PHEROMONE_BASE_CONST,
                pheromones + (FACTOR_ADDING_PHEROMONES * ESTIMATED_SHORTEST_LENGTH) / total_length.pow(
                    FACTOR_ADDING_PHEROMONES_EXP
                )
            )
        )

    fun flipExitNums() = this.copy(exitNumberStart = exitNumberEnd, exitNumberEnd = exitNumberStart)

    // Erst factor für Pheromones, dann für view, dann gemeinsam Gewichten.
    val probFactor: Double by lazy {
        pheromones.pow(AntConstants.FACTOR_PHEROMONES) * view.pow(
            AntConstants.FACTOR_DISTANCE
        )
    }
}