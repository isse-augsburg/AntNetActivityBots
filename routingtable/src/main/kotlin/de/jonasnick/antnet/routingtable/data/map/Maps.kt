package de.jonasnick.antnet.routingtable.data.map

import de.jonasnick.antnet.routingtable.data.AntConstants
import de.jonasnick.antnet.routingtable.data.AntManager

data class Pos(val x: Int, val y: Int)
data class FromTo(val from: Int, val to: Int)
data class SplitNode(val nodeID: Int, val masterNodeIDFrom: Int, val masterNodeIDTo: Int, val x: Int, val y: Int) {
    override fun toString() = "spl$nodeID"
}

data class MainNodeCon(val totalLength: Int, val paths: List<SplitPath>) {
    constructor(totalLength: Int, from: Int, to: Int, vararg paths: SplitPathPartial) : this(totalLength, paths.map {
        SplitPath(
            it.fromNode, from, it.toNode, to, it.splitLength,
            it.splitLength / totalLength.toDouble()
        )
    })
}


data class SplitPath(
    val fromNode: String,
    val fromID: Int,
    val toNode: String,
    val toID: Int,
    val splitLength: Int,
    val progress: Double
)

data class SplitPathPartial(
    val fromNode: String,
    val toNode: String,
    val splitLength: Int
)


/**
 * Used to hardcode maps with angles and fixed positions
 *
 */
object Maps {
    const val LENGTH = 3.0

    /**
     * not used
     */
    val unusedExits = mapOf(
        1 to listOf(0, 3),
        2 to listOf(0),
        3 to listOf(0, 1, 2),
        4 to listOf(3),
        5 to listOf(1),
        6 to listOf(1, 2)
    )

    /**
     * In case of a failure the ants are trying to go back to the start point using this map
     * (node id, exit number)
     */
    val returnToBaseMap = mutableMapOf(
        1 to 0,
        2 to 3,
        3 to 3,
        4 to 0,
        5 to 0,
        6 to 0
    )

    var fixedPosMap: MutableMap<Int, Pos>? = null
        private set

    // helpers for the ids
    private const val A = 1
    private const val B = 2
    private const val C = 3
    private const val D = 4
    private const val E = 5
    private const val F = 6

    // partial nodes at the corners in between two actual nodes
    private val SPL_BE_1 = SplitNode(0, B, E, -7, -14)
    private val SPL_BE_2 = SplitNode(1, B, E, -5, -14)

    private val SPL_DE_1 = SplitNode(2, D, E, -10, -10)
    private val SPL_DE_2 = SplitNode(3, D, E, -10, -12)
    private val SPL_DE_3 = SplitNode(4, D, E, -8, -12)
    private val SPL_DE_4 = SplitNode(5, D, E, -8, -10)

    private val SPL_EF_1 = SplitNode(6, E, F, -5, -7)
    private val SPL_EF_2 = SplitNode(7, E, F, -3, -7)

    private val SPL_DF_1 = SplitNode(8, D, F, -13, -4)
    private val SPL_DF_2 = SplitNode(9, D, F, -10, -4)
    private val SPL_DF_3 = SplitNode(10, D, F, -10, -6)
    private val SPL_DF_4 = SplitNode(11, D, F, -7, -6)
    private val SPL_DF_5 = SplitNode(12, D, F, -7, -4)


    /**
     * A list of all the split nodes
     */
    val splitNodes: MutableList<SplitNode> = mutableListOf(
        SPL_BE_1,
        SPL_BE_2,
        SPL_DE_1,
        SPL_DE_2,
        SPL_DE_3,
        SPL_DE_4,
        SPL_EF_1,
        SPL_EF_2,
        SPL_DF_1,
        SPL_DF_2,
        SPL_DF_3,
        SPL_DF_4,
        SPL_DF_5
    )

    /**
     * A list of the length of all connections between nodes as well as the partial path between them
     *
     * This data is only for the GUI and is not relevant for the underlying algorithm
     */
    val pathMap: MutableMap<FromTo, MainNodeCon> = mutableMapOf(
        FromTo(A, B) to MainNodeCon(
            30, A, B,
            SplitPathPartial("$A", "$B", 30)
        ),
        FromTo(A, D) to MainNodeCon(
            40, A, D,
            SplitPathPartial("$A", "$D", 40)
        ),
        FromTo(B, C) to MainNodeCon(
            10, B, C,
            SplitPathPartial("$B", "$C", 10)
        ),
        FromTo(B, E) to MainNodeCon(
            60, B, E,
            SplitPathPartial("$B", "$SPL_BE_1", 15),
            SplitPathPartial("$SPL_BE_1", "$SPL_BE_2", 20),
            SplitPathPartial("$SPL_BE_2", "$E", 25)

        ),
        FromTo(D, E) to MainNodeCon(
            90, D, E,
            SplitPathPartial("$D", "$SPL_DE_1", 15),
            SplitPathPartial("$SPL_DE_1", "$SPL_DE_2", 20),
            SplitPathPartial("$SPL_DE_2", "$SPL_DE_3", 20),
            SplitPathPartial("$SPL_DE_3", "$SPL_DE_4", 20),
            SplitPathPartial("$SPL_DE_4", "$E", 15)

        ),
        FromTo(E, F) to MainNodeCon(
            50, E, F,
            SplitPathPartial("$E", "$SPL_EF_1", 15),
            SplitPathPartial("$SPL_EF_1", "$SPL_EF_2", 20),
            SplitPathPartial("$SPL_EF_2", "$F", 15)

        ),
        FromTo(D, F) to MainNodeCon(
            170, D, F,
            SplitPathPartial("$D", "$SPL_DF_1", 45),
            SplitPathPartial("$SPL_DF_1", "$SPL_DF_2", 30),
            SplitPathPartial("$SPL_DF_2", "$SPL_DF_3", 20),
            SplitPathPartial("$SPL_DF_3", "$SPL_DF_4", 30),
            SplitPathPartial("$SPL_DF_4", "$SPL_DF_5", 20),
            SplitPathPartial("$SPL_DF_5", "$F", 25)

        )
    )


    /**
     * A hardcoded map used for the showcase
     *
     * This is the data the algorithm is going to work with
     */
    @Suppress("LocalVariableName")
    fun addShowcaseMap(manager: AntManager) {
        manager.nodeManager.apply {
            addConnection(
                A, B,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    3.0,
                    1,
                    3
                )
            )
            addConnection(
                A, D,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    4.0,
                    2,
                    0
                )
            )
            addConnection(
                B, C,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    1.0,
                    1,
                    3
                )
            )
            addConnection(
                B, E,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    6.0,
                    2,
                    0
                )
            )
            addConnection(
                D, E,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    9.0,
                    1,
                    3
                )
            )
            addConnection(
                D, F,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    17.0,
                    2,
                    3
                )
            )
            addConnection(
                E, F,
                Connection(
                    AntConstants.DEFAULT_PHEROMONES,
                    5.0,
                    2,
                    0
                )
            )

        }
        fixedPosMap = mutableMapOf(
            F to Pos(-3, -4),
            E to Pos(-5, -10),
            D to Pos(-13, -10),
            C to Pos(-3, -17),
            B to Pos(-7, -17),
            A to Pos(-13, -17)
        )
    }

    /*fun addExampleMap(manager: AntManager) {
        manager.nodeManager.apply {
            addConnection(0, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(0, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(0, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(1, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(1, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(2, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(3, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(3, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(3, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(4, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(5, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(6, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
        }
    }

    fun addSimpleMap(manager: AntManager) {
        manager.nodeManager.apply {
            addConnection(0, 1, 3 * LENGTH)
            addConnection(0, 3, 4 * LENGTH)
            addConnection(1, 2, 1 * LENGTH)
            addConnection(1, 4, 7 * LENGTH)
            addConnection(3, 4, 10 * LENGTH)
            addConnection(4, 5, 4 * LENGTH)
            addConnection(3, 5, 17 * LENGTH)
        }
    }

    fun addBigExampleMap(manager: AntManager) {
        manager.nodeManager.apply {
            addConnection(0, 1, 3.0)
            addConnection(0, 2, 9.0)
            addConnection(0, 4, 17.0)
            addConnection(1, 2, 11.0)
            addConnection(1, 3, 8.0)
            addConnection(2, 3, 4.0)
            addConnection(3, 4, 11.0)
            addConnection(3, 5, 12.0)
            addConnection(3, 6, 4.0)
            addConnection(4, 7, 12.0)
            addConnection(5, 6, 16.0)
            addConnection(6, 7, 1.0)
            addConnection(6, 8, 4.0)
            addConnection(4, 7, 9.0)
            addConnection(3, 7, 9.0)
            addConnection(6, 7, 8.0)
            addConnection(7, 8, 14.0)
            addConnection(6, 8, 17.0)
            addConnection(8, 9, 17.0)
            addConnection(9, 10, 11.0)
            addConnection(9, 11, 8.0)
            addConnection(9, 12, 4.0)
            addConnection(10, 11, 11.0)
            addConnection(11, 12, 12.0)
            addConnection(12, 13, 4.0)
            addConnection(12, 14, 12.0)
            addConnection(13, 15, 16.0)
            addConnection(15, 16, 1.0)
            addConnection(15, 17, 4.0)
            addConnection(17, 18, 9.0)
            addConnection(1, 13, 9.0)
            addConnection(17, 19, 8.0)
            addConnection(16, 19, 14.0)
            addConnection(13, 17, 17.0)
        }
    }

    fun addGiantMap(manager: AntManager) {
        manager.nodeManager.apply {
            addConnection(5, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(18, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(14, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(1, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(13, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(19, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(15, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(12, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(14, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(7, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(15, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(13, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(12, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(15, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(5, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(13, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(4, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(1, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(7, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(11, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(9, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(13, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(1, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(14, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(7, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(2, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(14, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(5, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(5, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(17, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(3, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(0, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(2, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(15, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(19, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(5, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(3, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(16, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(14, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(4, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(13, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(14, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(6, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(6, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(17, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(1, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(10, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(19, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(5, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(4, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(3, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(9, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(6, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(4, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(0, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(13, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(10, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(0, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(13, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(2, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(0, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(17, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(3, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(4, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(19, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(2, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(3, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(1, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(18, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(3, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(11, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(10, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(13, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(19, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(1, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(2, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(19, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(6, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(17, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(0, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(10, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(13, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(3, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(1, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(16, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(19, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(4, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(12, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(16, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(3, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(3, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(19, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(2, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(2, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(2, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(0, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(16, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(10, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(12, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(18, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(17, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(8, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(8, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(10, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(10, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(19, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(17, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(7, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(17, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(12, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(4, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(4, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(4, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(19, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(0, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(15, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(13, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(3, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(19, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(5, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(16, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(19, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(6, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(3, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(15, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(15, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(7, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(17, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(15, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(14, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(1, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(13, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(12, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(12, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(7, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(1, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(15, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(5, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(10, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(6, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(2, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(14, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(7, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(9, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(0, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(0, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(14, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(9, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(14, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(12, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(7, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(1, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(3, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(16, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(4, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(3, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(10, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(17, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(7, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(17, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(2, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(16, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(11, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(10, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(1, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(19, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(11, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(11, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(13, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(10, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(0, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(2, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(18, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(12, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(13, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(14, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(19, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(15, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(6, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(13, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(15, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(14, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(17, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(12, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(3, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(4, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(4, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(19, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(13, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(0, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(9, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(14, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(18, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(5, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(1, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(19, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(14, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(3, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(7, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(11, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(8, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(15, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(7, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(11, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(12, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(4, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(12, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(0, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(15, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(17, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(8, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(19, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(8, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(13, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(10, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(7, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(9, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(0, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(5, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(12, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(16, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(17, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(11, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(0, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(8, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(18, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(10, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(3, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(16, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(0, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(12, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(0, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(7, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(7, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(12, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(12, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(14, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(19, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(10, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(6, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(14, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(17, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(16, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(19, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(8, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(10, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(18, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(1, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(13, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(8, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(16, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(9, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(18, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(11, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(9, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(14, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(6, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(9, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(5, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(2, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(1, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(14, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(16, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(10, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(11, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(10, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(19, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(1, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(17, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(18, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(19, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(19, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(4, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(3, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(2, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(19, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(0, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(18, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(5, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(12, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(14, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(15, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(11, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(12, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(3, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(9, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(11, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(10, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(4, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(3, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(14, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(1, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(15, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(19, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(0, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(11, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(9, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(16, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(6, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(6, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(4, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(8, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(5, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(8, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(11, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(11, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(5, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(4, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(5, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(17, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(8, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(17, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(15, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(10, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(17, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(13, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(3, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(2, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(1, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(13, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(6, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(15, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(14, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(0, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(10, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(10, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(8, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(7, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(16, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(17, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(19, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(8, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(6, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(6, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(9, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(6, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(4, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(5, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(19, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(19, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(9, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(9, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(19, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(0, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(12, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(4, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(14, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(11, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(15, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(19, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(15, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(15, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(9, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(12, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(5, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(9, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(17, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(0, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(13, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(1, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(8, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(6, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(15, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(18, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(17, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(5, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(11, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(7, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(15, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(4, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(18, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(15, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(11, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(19, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(16, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(0, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(3, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(13, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(9, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(1, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(15, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(6, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(0, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(13, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(16, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(3, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(18, 17, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(8, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(19, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(13, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(12, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(18, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(7, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(11, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(8, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(3, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(7, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(16, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(0, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(10, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(2, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(9, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(4, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(12, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(13, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(6, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(14, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(9, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(19, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(3, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(10, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(8, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(1, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(13, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(7, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(18, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(11, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(14, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(9, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(8, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(2, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(6, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(4, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(19, 6, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(13, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(15, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(9, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(14, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(2, 2, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(14, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 4.0))
            addConnection(12, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(5, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(6, 15, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(2, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(10, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 18.0))
            addConnection(13, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(17, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(5, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(4, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(15, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(5, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(12, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(5, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(6, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(3, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(10, 7, Connection(AntConstants.DEFAULT_PHEROMONES, 9.0))
            addConnection(2, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(1, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 5.0))
            addConnection(0, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(10, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(6, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(18, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(6, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(4, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 14.0))
            addConnection(1, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(5, 0, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
            addConnection(0, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(13, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(7, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(18, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 6.0))
            addConnection(14, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 15.0))
            addConnection(8, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(16, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 17.0))
            addConnection(9, 12, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(4, 9, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(1, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(5, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(17, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(16, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(7, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(6, 13, Connection(AntConstants.DEFAULT_PHEROMONES, 3.0))
            addConnection(2, 16, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(13, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 11.0))
            addConnection(19, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(14, 8, Connection(AntConstants.DEFAULT_PHEROMONES, 10.0))
            addConnection(14, 19, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(6, 4, Connection(AntConstants.DEFAULT_PHEROMONES, 7.0))
            addConnection(10, 18, Connection(AntConstants.DEFAULT_PHEROMONES, 8.0))
            addConnection(18, 5, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(13, 1, Connection(AntConstants.DEFAULT_PHEROMONES, 1.0))
            addConnection(5, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 12.0))
            addConnection(0, 3, Connection(AntConstants.DEFAULT_PHEROMONES, 16.0))
            addConnection(18, 11, Connection(AntConstants.DEFAULT_PHEROMONES, 13.0))
            addConnection(19, 10, Connection(AntConstants.DEFAULT_PHEROMONES, 19.0))
            addConnection(18, 14, Connection(AntConstants.DEFAULT_PHEROMONES, 2.0))
        }
    }*/

}