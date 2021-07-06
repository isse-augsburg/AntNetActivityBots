package de.jonasnick.antnet.routingtable.data.ants

enum class AntState {
    MOVING,
    DEAD_END,
    FINISHED
}

/**
 * Interface that every Ant (virtual or not) should support
 */
interface IAnt {
    fun tick(): AntState
    val totalTimeTraveled: Double
    val id: Int

    val toNode: Int
    val fromNode: Int

    /** flag to keep it from changing position every time */
    var nodeChanged: Boolean

    val progress: Double
}