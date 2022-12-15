@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2022.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputFactory
import utils.InputNew
import utils.Signed
import utils.Template
import utils.Vector

const val day = 15
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<Reading>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    Sensor at x=2, y=18: closest beacon is at x=-2, y=15
    Sensor at x=9, y=16: closest beacon is at x=10, y=16
    Sensor at x=13, y=2: closest beacon is at x=15, y=3
    Sensor at x=12, y=14: closest beacon is at x=10, y=16
    Sensor at x=10, y=20: closest beacon is at x=10, y=16
    Sensor at x=14, y=17: closest beacon is at x=10, y=16
    Sensor at x=8, y=7: closest beacon is at x=2, y=10
    Sensor at x=2, y=0: closest beacon is at x=2, y=10
    Sensor at x=0, y=11: closest beacon is at x=2, y=10
    Sensor at x=20, y=14: closest beacon is at x=25, y=17
    Sensor at x=17, y=20: closest beacon is at x=21, y=22
    Sensor at x=16, y=7: closest beacon is at x=15, y=3
    Sensor at x=14, y=3: closest beacon is at x=15, y=3
    Sensor at x=20, y=1: closest beacon is at x=15, y=3
  """.trimIndent().split("\n").filter { it.isNotBlank() }

  init {
  }

  @Template("Sensor at x=#0, y=#1: closest beacon is at x=#2, y=#3")
  data class Reading(@Signed val sX: Int, @Signed val sY: Int, @Signed val bX: Int, @Signed val bY: Int) {
    val sensor = Vector(sX, sY)
    val beacon = Vector(bX, bY)
    val distance = sensor.cityDistanceTo(beacon)
  }

  var targetRow = 10

  fun initialize() {
    val lines = if (useRealData) {
      targetRow = 2000000
      val realInput = InputNew(year, day).readAsLines().filter { it.isNotBlank() }
      realInput
    } else {
      sampleInput
    }

    val inputFactory = InputFactory(Reading::class)
    input = lines.mapNotNull { inputFactory.lineToClass(it) }
  }

  fun part1() {
    val ranges = input.mapNotNull { rangeOnRow(it, targetRow) }
    val covered = mutableSetOf<Int>()
    ranges.forEach { range -> range.forEach { covered.add(it) } }
    input.filter { it.beacon.y == targetRow }.map { it.beacon.x }.forEach { x -> covered.remove(x) }
    println(covered.size)
  }

  fun part2() {

  }

  private fun rangeOnRow(reading: Reading, row: Int): IntRange? {
    val loc = Vector(reading.sensor.x, row)
    val distToRow = reading.sensor.cityDistanceTo(loc)
    val r = reading.distance - distToRow
    if (r < 0) {
      return null
    }

    return (loc.x - r)..(loc.x + r)
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
