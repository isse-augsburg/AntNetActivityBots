import java.io.File

object Encoder {
    /**
     * Creates the lines for a given roundabout id and number
     */
    fun roundaboutCode(out: Boolean, number: Int, id: Int, length: Int): List<Line> {
        println("id[$id]: " + Integer.toBinaryString(id shl 3))
        val value = (id shl 3) or (number shl 1) or if (out) 0 else 1
        return valueToLines(value, length)
    }

    /**
     * Converts an integer into a number
     */
    fun valueToLines(value: Int, lineCount: Int): List<Line> {
        if ((value shr (lineCount * 2)) > 0)
            throw java.lang.IllegalArgumentException("$value is too large to fit into $lineCount lines!")
        println("value[$value]: " + Integer.toBinaryString(value))
        val list = mutableListOf<Line>()
        for (i in 0 until lineCount) {
            val twoBit = value ushr (2 * i) and 0b11
            list += valueToLine(twoBit)
        }
        list.add(Line(1, 1, 1))
        list.reverse()

        return list
    }

    /**
     * Converts two bit value into Line
     */
    private fun valueToLine(byte: Int): Line {
        return when (byte) {
            0 -> Line(1, 1, 1)
            1 -> Line(1, 1, 0)
            2 -> Line(0, 1, 1)
            3 -> Line(1, 0, 1)
            else -> throw IllegalArgumentException("only 0,1,2,3 can be encoded")
        }
    }

    fun printFullCircle(id: Int) {
        println("###############  $id  ################################")
        for (i in 0..3) {
            println("-------- $i --------")
            println("--> Einfahrt [$i]")
            roundaboutCode(false, i, id, 4).forEach {
                println(it.readableString())
            }

            println("--> Ausfahrt [$i]")
            roundaboutCode(true, i, id, 4).forEach {
                println(it.readableString())
            }
        }
    }

    fun createFullCircle(id: Int, filename: String, length: Int = 4) {
        for (i in 0..3) {
            val sb = StringBuilder()
            var x = -1
            roundaboutCode(false, i, id, length).forEach {
                x++
                sb.append(it.toXML(170.25, x, 1))
            }
            roundaboutCode(true, i, id, length).forEach {
                sb.append(it.toXML(-5.25, x, -1))
                x--
            }

            File("$filename${id}_$i.svg").printWriter().use { out ->
                out.println(XMLHelper.embeddIntoBody(sb.toString(), "$id, $i"))
            }

            Thread.sleep(500)

            Runtime.getRuntime().exec("\"C:\\Program Files\\Inkscape\\inkscape.exe\" $filename${id}_$i.svg --export-pdf=$filename${id}_$i.pdf")
            Thread.sleep(1000)
            // File("$filename${id}_$i.svg").delete()
        }
    }
}

fun main() {
    for (i in 1..10)
        Encoder.createFullCircle(i, "testcircle", 4)

    // Encoder.printFullCircle(27)

    /*println("----- 27")
    val sb = StringBuilder()
    var x = -1
    Encoder.valueToLines(27, 4).forEach {
        x++
        sb.append(it.toXML(170.25, x, 1))
    }

    Encoder.valueToLines(27, 4).forEach {
        sb.append(it.toXML(-5.25, x, -1))
        x--
    }

    val myfile = File("test.svg")
    myfile.printWriter().use { out ->
        out.println(XMLHelper.embeddIntoBody(sb.toString(), 27, 0))
    }

    println("Wrote to ${myfile.absoluteFile}")
*/


    /*println("roundabout")
    Encoder.roundaboutCode(true, 1, 1, 4).forEach {
        println(it.readableString())
    }

    println("roundabout")
    Encoder.roundaboutCode(false, 1, 1, 4).forEach {
        println(it.readableString())
    }*/

}

data class Line(val left: Int, val middle: Int, val right: Int) {
    private val xIndent = 30.0
    fun readableString() = "$left$middle$right"

    /**
     * @param reversed 1 for normal, -1 for reversed
     * @param yPos bottom pos for normal, top pos for reversed
     */
    fun toXML(yPos: Double, xOffsetIndex: Int, reversed: Int): String {
        val sb = StringBuilder()
        if (left == 1) {
            sb.append(XMLHelper.createRect(xIndent + xOffsetIndex * 20, yPos - 27 * reversed))
        }
        if (middle == 1) {
            sb.append(XMLHelper.createRect(xIndent + xOffsetIndex * 20, yPos - 18 * reversed))
        }
        if (right == 1) {
            sb.append(XMLHelper.createRect(xIndent + xOffsetIndex * 20, yPos - 9 * reversed))
        }

        println(xOffsetIndex)
        if (xOffsetIndex % 2 == 0) {
            sb.append(XMLHelper.createRect(xIndent + 3 * reversed + xOffsetIndex * 20, yPos))
        }

        return sb.toString()
    }
}

object XMLHelper {
    private var rectID = 0
    fun createRect(xPos: Double, yPos: Double): String {
        return """
            <rect
                  id="rect${++rectID}"
                  width="20"
                  height="9"
                  style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.4565419;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:1"
                  x="$xPos"
                  y="$yPos"${'\n'}
                  />""".trimIndent()
    }

    fun embeddIntoBody(str: String, comment: String): String =
            """
    <?xml version="1.0" encoding="UTF-8" standalone="no"?>
    <!-- Created with Inkscape (http://www.inkscape.org/) -->

    <svg
    xmlns:dc="http://purl.org/dc/elements/1.1/"
    xmlns:cc="http://creativecommons.org/ns#"
    xmlns:rdf="http://www.w3.org/1999/02/22-rdf-syntax-ns#"
    xmlns:svg="http://www.w3.org/2000/svg"
    xmlns="http://www.w3.org/2000/svg"
    xmlns:sodipodi="http://sodipodi.sourceforge.net/DTD/sodipodi-0.dtd"
    xmlns:inkscape="http://www.inkscape.org/namespaces/inkscape"
    width="297mm"
    height="420mm"
    viewBox="0 0 297 420"
    version="1.1"
    id="svg8"
    inkscape:version="0.92.4 (5da689c313, 2019-01-14)"
    sodipodi:docname="BaseFile.svg">
    <defs
    id="defs2" />
    <sodipodi:namedview
    id="base"
    pagecolor="#ffffff"
    bordercolor="#666666"
    borderopacity="1.0"
    inkscape:pageopacity="0.0"
    inkscape:pageshadow="2"
    inkscape:zoom="0.76230764"
    inkscape:cx="508.90555"
    inkscape:cy="847.68055"
    inkscape:document-units="mm"
    inkscape:current-layer="layer1"
    showgrid="false"
    inkscape:window-width="1920"
    inkscape:window-height="1051"
    inkscape:window-x="-9"
    inkscape:window-y="-9"
    inkscape:window-maximized="1" />
    <metadata
    id="metadata5">
    <rdf:RDF>
    <cc:Work
    rdf:about="">
    <dc:format>image/svg+xml</dc:format>
    <dc:type
    rdf:resource="http://purl.org/dc/dcmitype/StillImage" />
    <dc:title></dc:title>
    </cc:Work>
    </rdf:RDF>
    </metadata>
    <g
    inkscape:label="Ebene 1"
    inkscape:groupmode="layer"
    id="layer1"
    transform="translate(0,123)">
    <path
    style="fill:none;stroke:#000000;stroke-width:0.42191914px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1"
    d="m 0,235.28904 c 172.04854,0 297,0 297,0"
    id="path863"
    inkscape:connector-curvature="0" />
    <path
    style="fill:none;stroke:#000000;stroke-width:0.42191917px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1"
    d="m 0,-61.677391 c 172.04854,0 297,0 297,0"
    id="path863-9"
    inkscape:connector-curvature="0" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.4565419;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1924"
    width="13"
    height="296.80396"
    x="189.75"
    y="-61.73008" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.45693508;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1926"
    width="13"
    height="297.31537"
    x="242.75"
    y="-61.954357" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.34142855;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1924-5"
    width="13"
    height="166"
    x="-194.25"
    y="0.14363711"
    transform="rotate(-90)" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.34142855;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1926-7"
    width="13"
    height="166"
    x="-141.25"
    y="6.4918963e-06"
    transform="rotate(-90)" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.34142855;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1924-0-3"
    width="13"
    height="166"
    x="-45.75"
    y="2.7755931e-15"
    transform="rotate(-90)" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.34142855;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1926-5-3"
    width="13"
    height="166"
    x="7.25"
    y="-4.4352895e-16"
    transform="rotate(-90)" />
    <rect
    style="display:inline;opacity:1;fill:#000000;fill-opacity:1;fill-rule:nonzero;stroke:none;stroke-width:0.15417022;stroke-linecap:butt;stroke-linejoin:miter;stroke-miterlimit:4;stroke-dasharray:none;stroke-dashoffset:0;stroke-opacity:0.9848485"
    id="rect1926-5-3-8"
    width="40"
    height="22"
    x="-19.75"
    y="211.808"
    transform="rotate(-90)" />
    <path
    style="fill:none;stroke:#000000;stroke-width:0.42191914px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1"
    d="M 0,-61.677391 C 0,110.37115 0,235.32261 0,235.32261"
    id="path863-90"
    inkscape:connector-curvature="0" />
    <path
    style="fill:none;stroke:#000000;stroke-width:0.42191914px;stroke-linecap:butt;stroke-linejoin:miter;stroke-opacity:1"
    d="m 297,-61.677391 c 0,172.048541 0,297.000001 0,297.000001"
    id="path863-90-3"
    inkscape:connector-curvature="0" />
    <text
    xml:space="preserve"
    style="font-style:normal;font-weight:normal;font-size:5.64444447px;line-height:1.25;font-family:sans-serif;letter-spacing:0px;word-spacing:0px;fill:#000000;fill-opacity:1;stroke:none;stroke-width:0.26458332"
    x="-123.5268"
    y="181.06427"
    id="text68"
    transform="rotate(-90)"><tspan
    sodipodi:role="line"
    x="-123.5268"
    y="181.06427"
    style="stroke-width:0.26458332"
    id="tspan94">$comment</tspan></text>
    $str
    </g>
    </svg>
     """.trimIndent()
}
