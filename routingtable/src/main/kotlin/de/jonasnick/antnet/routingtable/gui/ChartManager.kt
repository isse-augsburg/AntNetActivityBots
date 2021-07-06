package de.jonasnick.antnet.routingtable.gui

import de.jonasnick.antnet.routingtable.data.AntFinish
import org.knowm.xchart.XChartPanel
import org.knowm.xchart.XYChart
import org.knowm.xchart.XYChartBuilder
import org.knowm.xchart.XYSeries.XYSeriesRenderStyle
import org.knowm.xchart.style.Styler.LegendPosition
import java.awt.Color
import javax.swing.JSplitPane
import kotlin.math.max

/**
 * Manages the charts (scatter + line plot) using the library XChart
 *
 */
class ChartManager(
    private val iterations: List<Int>,
    private val values: List<Double>,
    private val scatterData: MutableList<AntFinish>,
    private val scatterDataReal: MutableList<AntFinish>
) {
    private val chart =
        XYChartBuilder().width(600)
            .height(500)
            .title("Average Ant Time")
            .xAxisTitle("Time (in Iteration)")
            .yAxisTitle("Average Total Length")
            .build()


    private val scatter: XYChart = XYChartBuilder()
        .width(600)
        .height(500)
        .title("Individual Total Time")
        .xAxisTitle("Time (in Iteration)")
        .yAxisTitle("Individual Total Length").build()

    private val scatterPanel = XChartPanel(scatter)
    private val xyPanel = XChartPanel(chart)

    private var scatterHasData = false
    private var scatterHasDataReal = false
    private var xyHasData = false

    init {
        // Customize Line Plot
        chart.styler.defaultSeriesRenderStyle = XYSeriesRenderStyle.Line
        chart.styler.isChartTitleVisible = false
        chart.styler.markerSize = 0
        chart.styler.legendPosition = LegendPosition.InsideNE

        // Customize Scatter
        scatter.styler.defaultSeriesRenderStyle = XYSeriesRenderStyle.Scatter
        scatter.styler.isChartTitleVisible = false
        scatter.styler.legendPosition = LegendPosition.InsideSW
        scatter.styler.markerSize = 6
        scatter.styler.seriesColors = arrayOf(Color.BLUE, Color.RED)
    }

    /**
     * Gets called every frame when there are changes to the ant finish data
     *
     * updates both graphs with new data
     *
     * At the start of the gui it waits till there is data available before instantiating the plots
     */
    fun update() {
        if (values.isNotEmpty()) {

            if (xyHasData) {
                chart.updateXYSeries("ants", iterations, values, null)
            } else {
                chart.addSeries("ants", iterations, values)
                xyHasData = true
            }
            xyPanel.revalidate()
            xyPanel.repaint()
        }

        val lastIter = max(scatterData.lastOrNull()?.iteration ?: 0, scatterDataReal.lastOrNull()?.iteration ?: 0)

        if (scatterData.isNotEmpty()) {
            // deletes all old data to just keep the last 10k Iterations to not clutter the view
            scatterData.removeIf { it.iteration < lastIter - 10_000 }

            val lIter = scatterData.asSequence().map { it.iteration.toDouble() }.toList().toDoubleArray()
            val lLen = scatterData.asSequence().map { it.length }.toList().toDoubleArray()

            if (scatterHasData) {
                scatter.updateXYSeries("antScatter", lIter, lLen, null)
            } else {
                scatter.addSeries("antScatter", lIter, lLen)
                scatterHasData = true
            }
        }

        if (scatterDataReal.isNotEmpty()) {
            scatterDataReal.removeIf { it.iteration < lastIter - 10_000 }

            val lIterReal = scatterDataReal.asSequence().map { it.iteration.toDouble() }.toList().toDoubleArray()
            val lLenReal = scatterDataReal.asSequence().map { it.length }.toList().toDoubleArray()

            if (scatterHasDataReal) {
                scatter.updateXYSeries("antScatterReal", lIterReal, lLenReal, null)
            } else {
                scatter.addSeries("antScatterReal", lIterReal, lLenReal)

                scatterHasDataReal = true
            }
        }

        if (scatterDataReal.isNotEmpty() || scatterData.isNotEmpty()) {
            scatterPanel.revalidate()
            scatterPanel.repaint()
        }


    }

    fun getAsPanel(): JSplitPane {
        val pane = JSplitPane(JSplitPane.VERTICAL_SPLIT, xyPanel, scatterPanel)
        pane.resizeWeight = 0.5
        return pane
    }
}