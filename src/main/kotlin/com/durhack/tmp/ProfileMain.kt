package com.durhack.tmp

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromStream
import java.nio.file.Path
import kotlin.io.path.inputStream
import kotlin.system.measureNanoTime

fun main()
{
    var pheremoneMap = PheromoneMap()

    val path = Path.of("./trails.json")
    pheremoneMap.trails = Json.decodeFromStream(path.inputStream())

    var repeats = 20
    var totalTime = 0.0
    repeat(3){
        pheremoneMap.makeCache()
    }
    repeat(repeats){
        totalTime += measureNanoTime { pheremoneMap.makeCache() }
    }

    val avgTime = totalTime / repeats * 1e-6
    AntLogger.info { "Avg Time: ${avgTime}ms" }
}