package aoc2018.day06

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.CharGrid
import utils.InputNew
import utils.Vector
import utils.packageToYearDay
import utils.unzip

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  var maxSum = -1

  val sampleInput = """
    1, 1
    1, 6
    8, 3
    3, 4
    5, 5
    8, 9
  """.trimIndent().split("\n")

  fun initialize() {
    maxSum = if (useRealData) 10_000 else 32

    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  class Region(val origin: Vector) {
    var size = 1

    fun nextRing(dist: Int): List<Vector> {
      val min = origin - Vector(dist, dist)
      val max = origin + Vector(dist, dist)

      val h = (min.y .. max.y).flatMap { yy ->
        listOf(Vector(min.x, yy), Vector(max.x, yy))
      }

      val v = (min.x .. max.x).flatMap { xx ->
        listOf(Vector(xx, min.y), Vector(xx, max.y))
      }

      return (h + v).toSortedSet().toList()
    }
  }

  fun part1() {
    val coords = input.map { it.split(", ").map { it.toInt() }.let {Vector(it[0], it[1])} }

    val (xs, ys) = coords.unzip()

    val (xmin, xmax) = xs.range()
    val (ymin, ymax) = ys.range()

    val min0 = Vector(xmin, ymin)
    val max0 = Vector(xmax, ymax)

    val min = Vector(xmin - 1, ymin - 1)
    val max = Vector(xmax + 1, ymax + 1)

    val grid = CharGrid(size = 20, '.')

    val regions = mutableMapOf<Vector, Long>()

    val infiniteRegions = mutableSetOf<Vector>()

    (min.y .. max.y).forEach { yy ->
      (min.x .. max.x).forEach { xx ->
        val location = Vector(xx, yy)
        val distances = coords.map { center ->
          center to center.cityDistanceTo(location)
        }.sortedBy { it.second }
        if (distances[0].second != distances[1].second) {
          // is unique
          regions[distances[0].first] = regions.getOrDefault(distances[0].first, 0) + 1
          if (!location.inBounds(min0, max0)) {
            infiniteRegions.add(distances.first().first)
          }
        }
      }
    }

    val finiteRegions = regions.filterNot { it.key in infiniteRegions }
    println(finiteRegions.maxByOrNull { it.value })
  }

  fun part2() {
    val coords = input.map { it.split(", ").map { it.toInt() }.let {Vector(it[0], it[1])} }

    val (xs, ys) = coords.unzip()

    val (xmin, xmax) = xs.range()
    val (ymin, ymax) = ys.range()

    val min = Vector(xmin - 1, ymin - 1)
    val max = Vector(xmax + 1, ymax + 1)

    var total = 0

    (min.y .. max.y).forEach { yy ->
      (min.x .. max.x).forEach { xx ->
        val location = Vector(xx, yy)
        val distances = coords.map { center ->
          center.cityDistanceTo(location)
        }
        val sum = distances.sum()
        if (sum < maxSum) {
          total += 1
        }
      }
    }

    println(total)
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

private fun List<Int>.range(): Pair<Int, Int> {
  return this.minOf { it } to this.maxOf { it }
}
