@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2022.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigInteger
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    val minX = sX - distance
    val maxX = sX + distance

    val minY = sY - distance
    val maxY = sY + distance

    val forwardStart = minX - sY
    val backwardStart = minX + sY

    val forwardEnd = maxX - sY
    val backwardEnd = maxX + sY

    override fun toString(): String {
      return "$sensor ($beacon): $minX, $minY  $maxX, $maxY, $forwardStart, $forwardEnd, $backwardStart, $backwardEnd"
    }

    fun toDiamond(): Diamond {
      val top = Vector(sensor.x, minY)
      val bottom = Vector(sensor.x, maxY)
      val left = Vector(minX, sensor.y)
      val right = Vector(maxX, sensor.y)

      return Diamond(top, left, bottom, right)
    }

    fun contains(location: Vector) = sensor.cityDistanceTo(location) <= distance
  }

  var targetRow = 10
  var bound = 20

  fun initialize() {
    val lines = if (useRealData) {
      targetRow = 2000000
      bound = 4000000
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

  data class Diamond(val top: Vector, val left: Vector, val bottom: Vector, val right: Vector)

  val diamonds = mutableStateListOf<Diamond>()
  val beacons = mutableStateListOf<Vector>()
  val sensors = mutableStateListOf<Vector>()

  var maxX by mutableStateOf(1000)
  var maxY by mutableStateOf(1000)
  var minX by mutableStateOf(0)
  var minY by mutableStateOf(0)

  var xRange by mutableStateOf(1)
  var yRange by mutableStateOf(1)

  val forwardStarts = mutableStateListOf<Int>()
  val forwardEnds = mutableStateListOf<Int>()

  val backwardStarts = mutableStateListOf<Int>()
  val backwardEnds = mutableStateListOf<Int>()
  val forwardCandidates = mutableStateListOf<Int>()
  val backwardCandidates = mutableStateListOf<Int>()

  val intersections = mutableStateListOf<Vector>()

  var answer by mutableStateOf(Vector(0, 0))

  fun part2Init() {
    // input = input.drop(20) // .take(1)
    // input = input.take(1)
    // println(input.joinToString("\n"))

    maxX = input.map { it.maxX }.maxOf { it }
    maxY = input.map { it.maxY }.maxOf { it }
    minX = input.map { it.minX }.minOf { it }
    minY = input.map { it.minY }.minOf { it }

    maxX = bound
    maxY = bound
    minY = 0
    minX = 0

    xRange = maxX - minX
    yRange = maxY - minY

    diamonds.addAll(input.map { it.toDiamond() })
    beacons.addAll(input.map { it.beacon })
    sensors.addAll(input.map { it.sensor })

    forwardStarts.addAll(input.map { it.forwardStart }.sorted())
    forwardEnds.addAll(input.map { it.forwardEnd }.sorted())

    // forwardCandidates.addAll(
    //   forwardEnds.mapNotNull { fe ->
    //     val isCandidate = forwardStarts.any { fs ->
    //       fs - fe == 2
    //     }
    //
    //     if (isCandidate) {
    //       fe + 1
    //     } else {
    //       null
    //     }
    //   }
    // )

    // println(forwardCandidates)

    backwardStarts.addAll(input.map { it.backwardStart }.sortedDescending())
    backwardEnds.addAll(input.map { it.backwardEnd }.sortedDescending())

    // backwardCandidates.addAll(
    //   backwardEnds.mapNotNull { be ->
    //     val isCandidate = backwardStarts.any { bs ->
    //       be - bs == 2
    //     }
    //
    //     if (isCandidate) {
    //       be - 1
    //     } else {
    //       null
    //     }
    //   }
    // )

    // // Find the intersections
    // forwardCandidates.forEach { fc ->
    //   backwardCandidates.forEach { bc ->
    //     if (fc < bc) {
    //       val dist = bc - fc
    //       val y = dist / 2
    //       val v = Vector(fc + y, y)
    //       // println("$fc to $bc => $dist  $v")
    //       intersections.add(v)
    //     }
    //   }
    // }
    //

    val forwards = mutableSetOf<Int>()
    forwardStarts.forEach {
      forwards.add(it - 1)

      forwards.add(it + 1)
      forwards.add(it)
    }
    forwardEnds.forEach {
      forwards.add(it + 1)

      forwards.add(it - 1)
      forwards.add(it)
    }

    val backwards = mutableSetOf<Int>()
    backwardStarts.forEach {
      backwards.add(it + 1)

      backwards.add(it - 1)
      backwards.add(it)
    }
    backwardEnds.forEach {
      backwards.add(it - 1)

      backwards.add(it + 1)
      backwards.add(it)
    }

    forwards.forEach { f ->
      backwards.forEach { b ->

        if (f < b) {
          val dist = b - f
          val y = dist / 2
          val v = Vector(f + y, y)
          if (!(v.x < 0 || v.x > bound || v.y < 0 || v.y > bound)) {
            intersections.add(v)
          }
        }
      }
    }

    val answers = mutableSetOf<Vector>()
    intersections.forEach { candidate ->
      if (candidate !in beacons) {
        if (input.none { reading ->
            reading.contains(candidate)
          }) {
          answers.add(candidate)
        }
      }
    }

    println(answers)

    answer = answers.first()

    val m = 4_000_000
    val b = m.toBigInteger()

    val result = (answer.x.toBigInteger() * b) + answer.y.toBigInteger()
    println(result)
  }

  fun part2() {
    beacons.addAll(input.map { it.beacon })

    for (y: Int in 0..bound) {
      for (x: Int in 0..bound) {
        val v = Vector(x, y)
        if (v !in beacons) {
          if (input.none { reading ->
              reading.contains(v)
            }) {
            answer = v
            println(answer)
            return
          }
        }
      }
    }
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
