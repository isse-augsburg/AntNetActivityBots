package de.jonasnick.antnet.routingtable.gui

import de.jonasnick.antnet.routingtable.data.AntManager
import de.jonasnick.antnet.routingtable.data.map.*
import java.awt.*
import javax.imageio.ImageIO
import javax.swing.*
import kotlin.math.min
import kotlin.math.roundToInt


/**
 * The Swing gui that combines all other gui parts
 */
class GuiSwing(val antManager: AntManager) : JFrame() {
    companion object {
        /**
         * Creates the gui and calls the isVisible function on the swing thread to avoid race conditions
         */
        fun createAndShowGUI(antManager: AntManager): GuiSwing {
            val frame = GuiSwing(antManager)

            SwingUtilities.invokeAndWait {
                frame.isVisible = true
            }

            return frame
        }
    }

    var sleepTimeMillis: Long = 450
    var sleepTimeNanos: Int = 0


    private val slider = JSlider(0, 2000, 1000)
    private val sliderBox = JTextField("$sleepTimeMillis", 10)

    private val inputNode1 = JTextField(10)
    private val inputNode2 = JTextField(10)
    private val inputLength = JTextField(10)
    private val inputPheromones = JTextField(10)
    private val btnAddEdge = JButton("Add Edge")
    private val btnRemoveEdge = JButton("Remove Edge")

    val gm = GraphManager(antManager.nodeManager, antManager)
    val chart = ChartManager(
        antManager.antFinishesXAvg,
        antManager.antFinishesYAvg,
        antManager.antFinishes,
        antManager.realAntFinishes
    )


    init {
        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE


        //-----------------------------
        // Lower Left editor panel
        //------------------------------

        val buttonPanel = JPanel(BorderLayout())

        // Middle Buttons
        val middlePanel = JPanel().apply {
            layout = GridLayout(2, 6)
            add(JLabel("Node1"))
            add(JLabel("Node2"))
            add(JLabel("Length"))
            add(JLabel("Pheromones"))
            add(JLabel(""))
            add(JLabel(""))

            add(inputNode1)
            add(inputNode2)
            add(inputLength)
            add(inputPheromones)
            add(btnAddEdge)
            add(btnRemoveEdge)

            /** Adds an edge to the [NodeManager] using the given pheromones; Fakes the split path using a direct connection */
            btnAddEdge.addActionListener {
                val n1 = inputNode1.text.toIntOrNull() ?: return@addActionListener
                val n2 = inputNode2.text.toIntOrNull() ?: return@addActionListener

                val length = inputLength.text.toDoubleOrNull() ?: return@addActionListener
                val phero = inputPheromones.text.toDoubleOrNull() ?: return@addActionListener

                antManager.nodeManager.addConnection(
                    n1, n2,
                    Connection(phero, length, 0, 0)
                )

                Maps.pathMap[FromTo(n1, n2)] = MainNodeCon(
                    length.toInt(), n1, n2,
                    SplitPathPartial("$n1", "$n2", length.toInt())
                )
            }

            /** Removes an edge from the [NodeManager]; also removes the split path not clean up the view */
            btnRemoveEdge.addActionListener {
                val n1 = inputNode1.text.toIntOrNull() ?: return@addActionListener
                val n2 = inputNode2.text.toIntOrNull() ?: return@addActionListener

                antManager.nodeManager.deleteConnection(n1, n2)
                val rem = Maps.pathMap.remove(FromTo(n1, n2))

                // removes the remaining blobs from the split paths
                rem?.paths?.forEach { split ->
                    Maps.splitNodes.removeIf {
                        (it.masterNodeIDFrom == split.fromID && it.masterNodeIDTo == split.toID)
                                || (it.masterNodeIDFrom == split.toID && it.masterNodeIDTo == split.fromID)
                    }
                }
                //TODO: Not hardcode
                if ((n1 == 2 && n2 == 5) || (n1 == 2 || n2 == 5)) {
                    Maps.returnToBaseMap[5] = 3
                }

                this@GuiSwing.gm.setupGraph()
            }
        }
        buttonPanel.add(middlePanel, BorderLayout.CENTER)

        // Slider + Slider input field
        // This sets the sleep time used in the loop
        val panel = JPanel().apply {
            sliderBox.setSize(20, 10)
            sliderBox.minimumSize = Dimension(20, 10)
            slider.setSize(100, 10)

            layout = FlowLayout()
            add(sliderBox.apply {
                addActionListener {
                    val v = sliderBox.text.toDoubleOrNull() ?: return@addActionListener

                    // sliderBox.text = v.toString()
                    if (v >= 1) {
                        slider.value = min(v.toInt() + 1000, 2000)
                    } else if (v >= 0) {
                        slider.value = ((v - v.toLong().toDouble()) * 1_000).roundToInt()
                    }
                    sleepTimeMillis = v.toLong()
                    sleepTimeNanos = ((v - v.toLong().toDouble()) * 1_000_000).roundToInt()
                }
            })

            add(slider.apply {
                addChangeListener {
                    val v = slider.value
                    if (v > 999) {
                        sleepTimeMillis = v.toLong() - 999
                        sleepTimeNanos = 0
                        sliderBox.text = sleepTimeMillis.toString()
                    } else {
                        sleepTimeMillis = 0
                        sleepTimeNanos = v * 1000
                        sliderBox.text = (sleepTimeNanos / 1_000_000.0).toString()
                    }
                }
            })


        }
        buttonPanel.add(panel, BorderLayout.SOUTH)

        // ---------------------
        // Create The Charts
        // ---------------------
        val leftPanel = JPanel(BorderLayout())
        leftPanel.add(buttonPanel, BorderLayout.SOUTH)
        leftPanel.add(chart.getAsPanel(), BorderLayout.CENTER)

        // ---------------------
        // Create The Graph
        // ---------------------
        //TODO: Fix The background image not showing somehow
        val backgroundPanel = BackgroundPanel(ImageIO.read(this.javaClass.getResource("/map.png")))
        val mapPanel = gm.createViewPanelForJPanel()
        mapPanel.background = Color(0, 0, 0, 0)
        backgroundPanel.add(mapPanel, BorderLayout.CENTER)


        val splitPanel = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, backgroundPanel)
        mapPanel.minimumSize = Dimension(200, 200)
        mapPanel.size = Dimension(200, 200)

        this.add(splitPanel)

        pack()
    }
}