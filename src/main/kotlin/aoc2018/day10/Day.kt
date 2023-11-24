@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2018.day10

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.CharGrid
import utils.InputFactory
import utils.InputNew
import utils.Signed
import utils.Template
import utils.Vector
import utils.packageToYearDay
import utils.range
import utils.unzip

class Day(private val scope: CoroutineScope) {
  private lateinit var points: List<Point>
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    position=< 9,  1> velocity=< 0,  2>
    position=< 7,  0> velocity=<-1,  0>
    position=< 3, -2> velocity=<-1,  1>
    position=< 6, 10> velocity=<-2, -1>
    position=< 2, -4> velocity=< 2,  2>
    position=<-6, 10> velocity=< 2, -2>
    position=< 1,  8> velocity=< 1, -1>
    position=< 1,  7> velocity=< 1,  0>
    position=<-3, 11> velocity=< 1, -2>
    position=< 7,  6> velocity=<-1, -1>
    position=<-2,  3> velocity=< 1,  0>
    position=<-4,  3> velocity=< 2,  0>
    position=<10, -3> velocity=<-1,  1>
    position=< 5, 11> velocity=< 1, -2>
    position=< 4,  7> velocity=< 0, -1>
    position=< 8, -2> velocity=< 0,  1>
    position=<15,  0> velocity=<-2,  0>
    position=< 1,  6> velocity=< 1,  0>
    position=< 8,  9> velocity=< 0, -1>
    position=< 3,  3> velocity=<-1,  1>
    position=< 0,  5> velocity=< 0, -1>
    position=<-2,  2> velocity=< 2,  0>
    position=< 5, -2> velocity=< 1,  2>
    position=< 1,  4> velocity=< 2,  1>
    position=<-2,  7> velocity=< 2, -2>
    position=< 3,  6> velocity=<-1, -1>
    position=< 5,  0> velocity=< 1,  0>
    position=<-6,  0> velocity=< 2,  0>
    position=< 5,  9> velocity=< 1, -2>
    position=<14,  7> velocity=<-2,  0>
    position=<-3,  6> velocity=< 2, -1>
  """.trimIndent().split("\n")

  @Template("position=< *#0, *#1> velocity=< *#2, *#3>")
  data class Dot(@Signed val x: Int, @Signed val y: Int, @Signed val vx: Int, @Signed val vy: Int)

  data class Point(val location: Vector, val velocity: Vector)

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    val inputFactory = InputFactory(Dot::class)

    points = input.mapNotNull { inputFactory.lineToClass<Dot>(it)?.toPoint() }
  }

  fun part1() {
    var lastPoints = points
    var lastArea = points.map { it.location }.computeArea()

    var seconds = 0

    while(true) {
      val newPoints = lastPoints.map { point ->
        Point(point.location + point.velocity, point.velocity)
      }
      val newArea = newPoints.map { it.location }.computeArea()

      if (newArea > lastArea) {
        break
      }
      lastArea = newArea
      lastPoints = newPoints
      seconds += 1
    }

    val grid = lastPoints.map { it.location }.toGrid()
    println(grid)
    println(seconds)
  }

  fun part2() {
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

private fun List<Vector>.toGrid(): CharGrid {
  val (xs, ys) = this.unzip()
  val (xmin, xmax) = xs.range()
  val (ymin, ymax) = ys.range()

  val x0 = xmin - 1
  val x1 = xmax + 1

  val y0 = ymin - 1
  val y1 = ymax + 1

  val corner = Vector(x0, y0)

  val grid = CharGrid(x1 - x0 + 1, y1 - y0 + 1)

  this.forEach { location ->
    val l = location - corner
    grid[l] = '#'
  }

  return grid
}

private fun List<Vector>.computeArea(): Long {
  val (xs, ys) = this.unzip()
  val (xmin, xmax) = xs.range()
  val (ymin, ymax) = ys.range()

  val a = (xmax.toLong() - xmin.toLong()) * (ymax.toLong() - ymin.toLong())
  return a
}

private fun Day.Dot.toPoint(): Day.Point {
  return Day.Point(Vector(x, y), Vector(vx, vy))
}
