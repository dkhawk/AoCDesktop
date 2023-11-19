@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2018.day03

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputFactory
import utils.InputNew
import utils.Template
import utils.Vector
import utils.packageToYearDay
import utils.unzip

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<Patch>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    #1 @ 1,3: 4x4
    #2 @ 3,1: 4x4
    #3 @ 5,5: 2x2
  """.trimIndent().split("\n")

  init {
  }

  @Template("#0 @ #1,#2: #3x#4")
  data class Patch(val id: Int, val x: Int, val y: Int, val width: Int, val height: Int)

  fun initialize() {
    val lines = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }.map { it.substring(1) }

    val inputFactory = InputFactory(Patch::class)
    input = lines.mapNotNull { inputFactory.lineToClass<Patch>(it) }
  }

  fun part1() {
    val fabric = mutableMapOf<Vector, Int>()
    input.forEach { patch ->
      patch.toCoordinatesList().forEach { location ->
        fabric[location] = fabric.getOrDefault(location, 0) + 1
      }
    }

    val bounds = getBounds(fabric.keys)

    val min = bounds.min - Vector(1, 1)
    val max = bounds.max + Vector(1, 1)

    // println((min.y..max.y).map { row ->
    //   (min.x..max.x).map { col ->
    //     val count = fabric.getOrDefault(Vector(col, row), 0)
    //     when {
    //       count == 0 -> '.'
    //       count == 1 -> 'o'
    //       count < 10 -> "$count".first()
    //       else -> '*'
    //     }
    //   }.joinToString("")
    // }.joinToString("\n"))

    val overlapping = (min.y..max.y).flatMap { row ->
      (min.x..max.x).map { col ->
        val count = fabric.getOrDefault(Vector(col, row), 0)
        if (count >= 2) 1 else 0
      }
    }.sum()

    println(overlapping)
  }

  private fun getBounds(coordinates: Set<Vector>): MinMax {
    val (xs, ys) = coordinates.unzip()
    val min = Vector(xs.minOf { it }, ys.minOf { it })
    val max = Vector(xs.maxOf { it }, ys.maxOf { it })
    return MinMax(min, max)
  }

  private data class MinMax(
    val min: Vector,
    val max: Vector,
  )

  fun part2() {
    val fabric = mutableMapOf<Vector, MutableList<Int>>()
    input.forEach { patch ->
      patch.toCoordinatesList().forEach { location ->
        fabric.getOrPut(location) { mutableListOf() }.add(patch.id)
      }
    }

    val patch = input.first { patch ->
      patch.toCoordinatesList().map { fabric.getValue(it) }.all { it.size == 1 }
    }

    println(patch)
  }

  fun execute() {
    job?.cancel()
    job = scope.launch {
      running = true
      running = false
    }
  }

  fun step() {
  }

  fun stop() {
    job?.cancel()
    running = false
  }

  fun reset() {
    stop()
  }

  fun updateDataSource(useRealData: Boolean) {
    this.useRealData = useRealData
    initialize()
    reset()
  }
}

private fun Pair<Int, Int>.toVector() = Vector(first, second)

private fun Day.Patch.toCoordinatesList(): List<Vector> {
  return (y until(y + height)).flatMap { r ->
    (x until(x + width)).map { c -> (c to r).toVector() }
  }
}
