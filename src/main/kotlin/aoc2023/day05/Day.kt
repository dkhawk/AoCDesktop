package aoc2023.day05

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  var part: Int = 0
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    seeds: 79 14 55 13

    seed-to-soil map:
    50 98 2
    52 50 48

    soil-to-fertilizer map:
    0 15 37
    37 52 2
    39 0 15

    fertilizer-to-water map:
    49 53 8
    0 11 42
    42 0 7
    57 7 4

    water-to-light map:
    88 18 7
    18 25 70

    light-to-temperature map:
    45 77 23
    81 45 19
    68 64 13

    temperature-to-humidity map:
    0 69 1
    1 0 69

    humidity-to-location map:
    60 56 37
    56 93 4
  """.trimIndent().split("\n")

  var sampleInput2 : List<String>? = null

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else (
      if (sampleInput2 != null && part == 2)
        sampleInput2!!
      else
        sampleInput
    )
  }

  data class RangeMap(val destinationStart: Long, val sourceStart: Long, val range: Long) {
    fun remap(value: Long) = (value - sourceStart) + destinationStart
  }

  private fun RangeMap(line: String): RangeMap {
    val parts = line.split(' ').map { it.toLong() }
    return RangeMap(parts[0], parts[1], parts[2])
  }

  private val rangeMaps = mutableMapOf<String, MutableList<RangeMap>>()

  val progression = listOf(
    "seed-to-soil",
    "soil-to-fertilizer",
    "fertilizer-to-water",
    "water-to-light",
    "light-to-temperature",
    "temperature-to-humidity",
    "humidity-to-location",
  )

  fun part1() {
    val seeds = parseSeeds(input)
    buildRangeMaps(input)

    /*
    Seed 79, soil 81, fertilizer 81, water 81, light 74, temperature 78, humidity 78, location 82.
    Seed 14, soil 14, fertilizer 53, water 49, light 42, temperature 42, humidity 43, location 43.
    Seed 55, soil 57, fertilizer 57, water 53, light 46, temperature 82, humidity 82, location 86.
    Seed 13, soil 13, fertilizer 52, water 41, light 34, temperature 34, humidity 35, location 35.
     */

    // val m = rangeMaps["fertilizer-to-water"]!!
    // println(remap(m, 53))
    //
    // return
    val maps = progression.map { rangeMaps.getValue(it) }

    val closest = seeds.minOf {seed ->
      // print("$seed -> ")
      val location = maps.fold(seed) { value, map ->
        remap(map, value) // .also { print("$it, ") }
      }
      // println()
      location
    }

    println(closest)
  }

  private fun buildRangeMaps(input1: List<String>) {
    var currentRangeMapList = mutableListOf<RangeMap>()
    val iterator = input1.drop(1).iterator()
    while (iterator.hasNext()) {
      val line = iterator.next()
      if (line.isBlank()) {
        continue
      }
      if (line.first().isLetter()) {
        val (mapName, _) = line.split(' ')
        currentRangeMapList = mutableListOf<RangeMap>()
        rangeMaps[mapName] = currentRangeMapList
      }
      if (line.first().isDigit()) {
        currentRangeMapList.add(
          RangeMap(line)
        )
      }
    }
  }

  private fun parseSeeds(input1: List<String>): List<Long> {
    val seeds = input1.first().split(": ").last().split(' ').map { it.trim().toLong() }
    return seeds
  }

  private fun remap(map: List<RangeMap>, value: Long): Long {
    return map.find { value in it.sourceStart..(it.sourceStart + it.range) }?.remap(value) ?: return value
  }

  fun part2() {
    val seeds = parseSeeds(input)
    buildRangeMaps(input)

    /*
    Seed 79, soil 81, fertilizer 81, water 81, light 74, temperature 78, humidity 78, location 82.
    Seed 14, soil 14, fertilizer 53, water 49, light 42, temperature 42, humidity 43, location 43.
    Seed 55, soil 57, fertilizer 57, water 53, light 46, temperature 82, humidity 82, location 86.
    Seed 13, soil 13, fertilizer 52, water 41, light 34, temperature 34, humidity 35, location 35.
     */

    // val m = rangeMaps["fertilizer-to-water"]!!
    // println(remap(m, 53))
    //
    // return
    val maps = progression.reversed().map { rangeMaps.getValue(it) }

    val goal = 0

    val closest = seeds.minOf {seed ->
      // print("$seed -> ")
      val location = maps.fold(seed) { value, map ->
        remap(map, value) // .also { print("$it, ") }
      }
      // println()
      location
    }

    println(closest)
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
