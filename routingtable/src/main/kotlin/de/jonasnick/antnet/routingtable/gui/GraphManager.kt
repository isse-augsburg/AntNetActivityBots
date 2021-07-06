package de.jonasnick.antnet.routingtable.gui

import de.jonasnick.antnet.routingtable.data.AntManager
import de.jonasnick.antnet.routingtable.data.ants.RealAnt
import de.jonasnick.antnet.routingtable.data.map.FromTo
import de.jonasnick.antnet.routingtable.data.map.Maps
import de.jonasnick.antnet.routingtable.data.map.NodeManager
import org.graphstream.graph.Edge
import org.graphstream.graph.Node
import org.graphstream.graph.implementations.SingleGraph
import org.graphstream.ui.spriteManager.Sprite
import org.graphstream.ui.spriteManager.SpriteManager
import org.graphstream.ui.swingViewer.ViewPanel
import org.graphstream.ui.view.Viewer

/**
 * Manages the Graph displayed on the right side of the GUI using the library GraphStream
 */
class GraphManager(private val manager: NodeManager, val antManager: AntManager) {
    /**
     * Using companion object to only set Property once and load before graph
     */
    companion object {
        init {
            System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer")
        }
    }

    private val graph = SingleGraph("Tutorial 1", false, true)
    val sm = SpriteManager(graph)

    /**
     * Setup reactivity between the [NodeManager] and this
     */
    init {
        setupGraph()

        manager.addChangeListener {
            if (it.propertyName == "delete") {
                val old = it.oldValue as Pair<Int, Int>
                graph.removeEdge<Edge>("${old.first}_${old.second}")
                graph.removeEdge<Edge>("${old.second}_${old.first}")
            } else if (it.propertyName == "add") {
                val new = it.newValue as Pair<Int, Int>
                val id = "${new.first}_${new.second}";
                val edge = graph.addEdge<Edge>(id, "" + new.first, "" + new.second)
                edge.addAttribute("ui.color", 0.0)

                graph.getNode<Node>("" + new.first)?.addAttribute("ui.label", "Node ${new.first}")
                graph.getNode<Node>("" + new.second)?.addAttribute("ui.label", "Node ${new.second}")
            }

        }
    }

    /**
     * Would display the graph as a standalone gui (not desired)
     */
    fun display() = graph.display()

    /**
     * Creates the ViewPanel that can be embedded into the JavaSwing GUI
     */
    fun createViewPanelForJPanel(): ViewPanel {
        val viewer = Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD)
        if (Maps.fixedPosMap == null) viewer.enableAutoLayout()

        return viewer.addDefaultView(false)
    }

    /**
     * Initial setup of the graphical information of the graph
     *
     * sets the icon and sizes
     * sets the color between the edges are changing depending on pheromones
     */
    fun setAttributes() {
        graph.addAttribute(
            "ui.stylesheet", """
        |sprite {
	    |   shape: box;
	    |   fill-mode: image-scaled;
        |}
        |
        |sprite.carred {
        |   size: 20px, 20px;
        |   fill-image: url('carred.png');
        |   z-index: 10;
        |}
        |sprite.carblue {
        |   size: 16px, 16px;
        |   fill-image: url('car.png');
        |}
        |
        |sprite.pin {
        |   size: 16px, 26px;
        |   fill-image: url('mapPinSmall.png');
        |}
        |
        |edge {
	    |   fill-mode: dyn-plain;
	    |   fill-color: black, green;
        |   text-background-mode: plain;
        |}
        |
        |
        |
        |""".trimMargin()
        )

        graph.addAttribute("ui.quality")
        graph.addAttribute("ui.antialias")
    }

    /**
     * Returns the given edge and the Progress that is made on that given Edge
     *
     * This is used when there are partial connections to determine on which edge in the graph they are on
     */
    private fun getEffectiveEdge(from: Int, to: Int, progress: Double): Pair<Edge, Double>? {
        var newProgress = progress
        var con = Maps.pathMap[FromTo(from, to)]
        if (con == null) {
            con = Maps.pathMap[FromTo(to, from)]
                    //?: throw NoSuchElementException("Tried to access connection that doesn't exist $from:$to")
                ?: return null
            newProgress = 1 - progress
        }

        var currentMaxProgress = 0.0
        for (path in con.paths) {
            if (newProgress >= currentMaxProgress && newProgress <= currentMaxProgress + path.progress + 0.00000001) {
                val edge: Edge = graph.getEdge("${path.fromNode}_${path.toNode}")
                // ?: throw NoSuchElementException("Edge ${path.fromNode}_${path.toNode} does not exist (anymore)")
                    ?: return null

                return Pair(
                    edge,
                    (newProgress - currentMaxProgress) / path.progress
                )
            }
            currentMaxProgress += path.progress
        }

        throw NoSuchElementException("Edge not in range")
    }

    /**
     * Creates and sets up the graph
     * All nodes and connections are being created and set
     * all sprites are being spawned (start/finish + ants)
     */
    fun setupGraph() {
        graph.clear()
        setAttributes()

        manager.foreachNode { id ->
            val node = graph.addNode<Node>("" + id)
            node.addAttribute("ui.label", "Node $id")

            Maps.fixedPosMap?.let {
                it[id]?.let { pos ->
                    node.addAttribute("x", pos.x)
                    node.addAttribute("y", pos.y)
                }
            }
        }

        for (splitNode in Maps.splitNodes) {
            val node = graph.addNode<Node>("$splitNode")
            node.addAttribute("x", splitNode.x)
            node.addAttribute("y", splitNode.y)
        }

        for (value in Maps.pathMap.values) {
            for (path in value.paths) {
                val edge = graph.addEdge<Edge>("${path.fromNode}_${path.toNode}", path.fromNode, path.toNode)
                edge.addAttribute("ui.color", 0.0)
            }
        }

        synchronized(antManager.ants) {
            antManager.ants.forEach {
                sm.addSprite("" + it.id).apply {
                    addAttribute("ui.class", "carblue")
                }
            }
        }

        sm.addSprite("start").apply {
            attachToNode("" + antManager.startNode)
            addAttribute("ui.class", "pin")
        }

        sm.addSprite("end").apply {
            attachToNode("" + antManager.targetNode)
            addAttribute("ui.class", "pin")
        }

        updateGraph()
    }

    /**
     * This is called every frame to update the graph:
     * - pheromones are being updated on each connection
     * - ant sprites are moved to the new location (delegated to [updateAnts])
     */
    fun updateGraph() {
        var min = Double.MAX_VALUE
        var max = 0.0
        manager.foreachConnectionUnique { _, _, con ->
            if (con.pheromones < min)
                min = con.pheromones

            if (con.pheromones > max)
                max = con.pheromones
        }

        manager.foreachConnectionUnique { node1, node2, con ->
            val nodeCon = Maps.pathMap[FromTo(node1, node2)]
                ?: throw NoSuchElementException("There is no such connection in the Map, $node1 : $node2")

            var flag = true
            for (path in nodeCon.paths) {
                val edge = graph.getEdge<Edge>("${path.fromNode}_${path.toNode}") ?: continue

                edge.addAttribute("ui.color", (con.pheromones - min) / (max - min))

                if (flag) {
                    edge.addAttribute("ui.label", "" + con.distance + " / " + String.format("%.3g", con.pheromones))
                    flag = false
                }

            }


        }

        updateAnts()
    }

    /**
     * This is called every tick to update the ants and move them to the current location
     *
     * This respects the partial paths and uses the effective edge
     */
    fun updateAnts() {
        synchronized(antManager.ants) {
            for (it in antManager.ants) {

                var sprite: Sprite? = sm.getSprite("" + it.id)
                if (sprite == null) {
                    val isReal = it is RealAnt

                    sprite = sm.addSprite("" + it.id).apply {
                        addAttribute("ui.class", if (isReal) "carred" else "carblue")
                    }
                }

                val edgeProg = getEffectiveEdge(it.fromNode, it.toNode, it.progress)
                if (edgeProg != null && sprite != null) {
                    sprite.attachToEdge(edgeProg.first.id)
                    sprite.setPosition(edgeProg.second)
                }
            }
        }
    }
}