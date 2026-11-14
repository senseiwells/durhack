import org.openrndr.application
import org.openrndr.color.ColorRGBa
import org.openrndr.extra.gui.GUI
import org.openrndr.extra.parameters.ActionParameter
import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.ColorParameter
import org.openrndr.extra.parameters.DoubleParameter
import org.openrndr.extra.parameters.IntParameter
import org.openrndr.extra.parameters.XYParameter
import org.openrndr.math.Polar
import org.openrndr.math.Vector2

fun main() = application {
    configure {
        width = 1000
        height = 700
    }

    program {
        val gui = GUI()
        gui.compartmentsCollapsedByDefault = false

        val settings = object {
            @DoubleParameter("radius", 5.0, 300.0)
            var radius = 80.0

            @IntParameter("count", 1, 50)
            var count = 12

            @ColorParameter("fill")
            var fill = ColorRGBa.PINK

            @BooleanParameter("outline")
            var outline = true

            @XYParameter("center", minX = 0.0, maxX = 1000.0, minY = 0.0, maxY = 700.0)
            var center = Vector2(500.0, 350.0)

            @ActionParameter("randomize")
            fun randomize() {
                radius = (5..300).random().toDouble()
                count = (1..50).random()
            }
        }

        gui.add(settings, "Shape")
        extend(gui)

        extend {
            drawer.clear(ColorRGBa.BLACK)
            drawer.fill = settings.fill
            drawer.stroke = if (settings.outline) ColorRGBa.WHITE else null

            val positions = List(settings.count) { i ->
                val angle = i * 360.0 / settings.count
                Vector2(settings.center.x, height - settings.center.y) + Polar(angle, settings.radius * 2.0).cartesian
            }
            drawer.circles(positions, settings.radius / 3.0)
        }
    }
}
