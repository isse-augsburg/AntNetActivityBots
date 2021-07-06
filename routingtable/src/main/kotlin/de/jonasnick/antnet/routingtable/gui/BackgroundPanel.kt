package de.jonasnick.antnet.routingtable.gui

import java.awt.*
import javax.swing.JComponent
import javax.swing.JPanel
import javax.swing.JScrollPane

/**
 * Not working yet!!
 *
 * From: https://tips4java.wordpress.com/2008/10/12/background-panel/
 *
 *
 * Support custom painting on a panel in the form of
 *
 *
 * a) images - that can be scaled, tiled or painted at original size
 * b) non solid painting - that can be done by using a Paint object
 *
 *
 * Also, any component added directly to this panel will be made
 * non-opaque so that the custom painting can show through.
 */
class BackgroundPanel : JPanel {
    private var painter: Paint? = null
    private var image: Image? = null
    private var style = SCALED
    private var alignmentXShadow = 0.5f
    private var alignmentYShadow = 0.5f
    private var isTransparentAdd = true

    /*
     *  Set image as the background with the specified style
     */
    /*
     *  Set image as the background with the SCALED style
     */
    @JvmOverloads
    constructor(image: Image?, style: Int = SCALED) {
        setImage(image)
        setStyle(style)
        layout = BorderLayout()
    }

    /*
     *  Set image as the background with the specified style and alignment
     */
    constructor(image: Image?, style: Int, alignmentX: Float, alignmentY: Float) {
        setImage(image)
        setStyle(style)
        setImageAlignmentX(alignmentX)
        setImageAlignmentY(alignmentY)
        layout = BorderLayout()
    }

    /*
     *  Use the Paint interface to paint a background
     */
    constructor(painter: Paint?) {
        setPaint(painter)
        layout = BorderLayout()
    }

    /*
     *	Set the image used as the background
     */
    fun setImage(image: Image?) {
        this.image = image
        repaint()
    }

    /*
     *	Set the style used to paint the background image
     */
    fun setStyle(style: Int) {
        this.style = style
        repaint()
    }

    /*
     *	Set the Paint object used to paint the background
     */
    fun setPaint(painter: Paint?) {
        this.painter = painter
        repaint()
    }

    /*
     *  Specify the horizontal alignment of the image when using ACTUAL style
     */
    fun setImageAlignmentX(alignmentX: Float) {
        this.alignmentXShadow = if (alignmentX > 1.0f) 1.0f else if (alignmentX < 0.0f) 0.0f else alignmentX
        repaint()
    }

    /*
     *  Specify the horizontal alignment of the image when using ACTUAL style
     */
    fun setImageAlignmentY(alignmentY: Float) {
        this.alignmentYShadow = if (alignmentY > 1.0f) 1.0f else if (alignmentY < 0.0f) 0.0f else alignmentY
        repaint()
    }

    /*
     *  Override to provide a preferred size equal to the image size
     */
    override fun getPreferredSize(): Dimension {
        return if (image == null) super.getPreferredSize() else Dimension(
            image!!.getWidth(null),
            image!!.getHeight(null)
        )
    }

    /*
     *  Override method so we can make the component transparent
     */
    /*
     *  Override method so we can make the component transparent
     */
    @JvmOverloads
    fun add(component: JComponent, constraints: Any? = null) {
        if (isTransparentAdd) {
            makeComponentTransparent(component)
        }
        super.add(component, constraints)
    }

    /*
     *  Controls whether components added to this panel should automatically
     *  be made transparent. That is, setOpaque(false) will be invoked.
     *  The default is set to true.
     */
    fun setTransparentAdd(isTransparentAdd: Boolean) {
        this.isTransparentAdd = isTransparentAdd
    }

    /*
     *	Try to make the component transparent.
     *  For components that use renderers, like JTable, you will also need to
     *  change the renderer to be transparent. An easy way to do this it to
     *  set the background of the table to a Color using an alpha value of 0.
     */
    private fun makeComponentTransparent(component: JComponent) {
        component.isOpaque = false
        if (component is JScrollPane) {
            val viewport = component.viewport
            viewport.isOpaque = false
            val c = viewport.view
            if (c is JComponent) {
                c.isOpaque = false
            }
        }
    }

    /*
     *  Add custom painting
     */
    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)

        //  Invoke the painter for the background
        if (painter != null) {
            val d = size
            val g2 = g as Graphics2D
            g2.paint = painter
            g2.fill(Rectangle(0, 0, d.width, d.height))
        }

        //  Draw the image
        if (image == null) return
        when (style) {
            SCALED -> drawScaled(g)
            TILED -> drawTiled(g)
            ACTUAL -> drawActual(g)
            else -> drawScaled(g)
        }
    }

    /*
     *  Custom painting code for drawing a SCALED image as the background
     */
    private fun drawScaled(g: Graphics) {
        val d = size
        g.drawImage(image, 0, 0, d.width, d.height, null)
    }

    /*
     *  Custom painting code for drawing TILED images as the background
     */
    private fun drawTiled(g: Graphics) {
        val d = size
        val width = image!!.getWidth(null)
        val height = image!!.getHeight(null)
        run {
            var x = 0
            while (x < d.width) {
                run {
                    var y = 0
                    while (y < d.height) {
                        g.drawImage(image, x, y, null, null)
                        y += height
                    }
                }
                x += width
            }
        }
    }

    /*
     *  Custom painting code for drawing the ACTUAL image as the background.
     *  The image is positioned in the panel based on the horizontal and
     *  vertical alignments specified.
     */
    private fun drawActual(g: Graphics) {
        val d = size
        val insets = insets
        val width = d.width - insets.left - insets.right
        val height = d.height - insets.top - insets.left
        val x = (width - image!!.getWidth(null)) * alignmentXShadow
        val y = (height - image!!.getHeight(null)) * alignmentYShadow
        g.drawImage(image, x.toInt() + insets.left, y.toInt() + insets.top, this)
    }

    companion object {
        const val SCALED = 0
        const val TILED = 1
        const val ACTUAL = 2
    }
}