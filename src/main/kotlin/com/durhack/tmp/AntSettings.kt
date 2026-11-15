package com.durhack.tmp

import org.openrndr.extra.parameters.BooleanParameter
import org.openrndr.extra.parameters.IntParameter

object CoreSettings {

    @IntParameter("ants", 1, 1000)
    var ants = 100

    @IntParameter("food", 1, 100)
    var food = 30
    @IntParameter("random food distance", 30, 300)
    var foodRandomDistance = 200
    @IntParameter("food amount", 10, 1000)
    var foodAmount = 500

    @IntParameter("tps", 1, 1000)
    var tps = 20

    @IntParameter("path smoothing", 2, 16)
    var pathSmoothing = 4
    @IntParameter("clamping angle", 2, 90)
    var clampingAngle = 10

    // @XYParameter("center", minX = 0.0, maxX = 1000.0, minY = 0.0, maxY = 700.0)
    // var center = Vector2(500.0, 350.0)
}

object DebugSettings {
    @BooleanParameter("cache outline")
    var cache = true

    @BooleanParameter("trails")
    var trails = false

}