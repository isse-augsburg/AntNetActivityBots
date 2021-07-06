package de.jonasnick.antnet.routingtable.data

/**
 * Config file for setting most settings within the algorithm
 */
object AntConstants {
    /**
     * Amount of virtual ants that should be used
     */
    const val ANT_COUNT = 20 // 200

    /**
     * Amount of real ants that are being used
     */
    const val ANT_COUNT_REAL = 0

    /**
     * The (estimated) shortest path in the given graph from start to finish
     * Needed for a better estimation of the pheromones that are set
     */
    const val ESTIMATED_SHORTEST_LENGTH: Double = 140.0

    /**
     * How much the Pheromones on the path are being factored in (in contrast to distance)
     */
    const val FACTOR_PHEROMONES: Double = 1.0

    /**
     * How much the Distance on the path are being factored in (in contrast to pheromones)
     */
    const val FACTOR_DISTANCE: Double = 0.1

    /**
     * Multiplicative Factor for adding the pheromones once the ants finished their path
     */
    const val FACTOR_ADDING_PHEROMONES: Double = 10.0

    /**
     * Exponential Factor for adding the pheromones once the ants finished their path
     */
    const val FACTOR_ADDING_PHEROMONES_EXP: Double = 2.0
    // const val FACTOR_EVAPORATION: Double = 0.0001 * ANT_COUNT
    // const val FACTOR_EVAPORATION: Double = (1 - 0.993442778) * ANT_COUNT // 0.00001 * ANT_COUNT

    /**
     * Factor how much the pheromones are evaporated each tick
     * Pheromones are multiplied by (1 - n) each tick
     */
    const val FACTOR_EVAPORATION: Double = 0.0005 * (ANT_COUNT + ANT_COUNT_REAL)

    /**
     * Unused currently
     * Reserved for a additive update to the pheromones
     */
    const val ADDITIVE_UPDATE: Double = 0.2

    /**
     * Lower limit the pheromones can't fall below
     */
    const val PHEROMONE_BASE_CONST: Double = 1e-4

    /**
     * Default pheromones the Connections have when the graph is started
     */
    const val DEFAULT_PHEROMONES: Double = 0.2
}