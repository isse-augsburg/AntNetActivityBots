package de.jonasnick.antnet.routingtable.data.map

import kotlin.random.Random

/**
 * Helper function that completely randomly generates a graph
 * could be great, could not work at all
 */
fun main() {
    repeat(500) {
        println(
            "addConnection(${Random.nextInt(0, 20)}, ${
                Random.nextInt(
                    0,
                    20
                )
            }, Connection(AntConstants.DEFAULT_PHEROMONES, ${Random.nextInt(1, 20)}.0))"
        )
    }
}