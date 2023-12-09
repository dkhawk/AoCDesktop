package aoc2023.day05

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.lang.Long.max
import java.lang.Long.min
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
    fun toSourceRange() = sourceStart until (sourceStart + range)
    fun toDestinationRange() = destinationStart until (destinationStart + range)
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

  data class MapperRanges(val source: LongRange, val destination: LongRange) {
    fun remapValue(value: Long) = value + (destination.first - source.first)
  }

  fun part2() {
    // println((0L..10L).splitBy(11L..20L))
    // println((11L..20L).splitBy(0L..10L))
    // println((0L..10L).splitBy(5L..15L))
    // println((5L..7L).splitBy(0L..10L))
    // showSplit(0L..10L, 11L..20L)
    // showSplit(11L..20L, 0L..10L)
    // showSplit(0L..10L, 5L..15L)
    // showSplit(5L..7L, 0L..10L)
    // showSplit(5L..15L, 0L..10L)
    //
    // return


    val seeds = parseSeeds(input)
    buildRangeMaps(input)

    // rangeMaps.forEach { (key, value) ->
    //   println(key)
    //   println(value.joinToString("\n"))
    //   println()
    // }

    val mappers = progression.map { rangeMaps.getValue(it) }.map { item -> item.map {MapperRanges(it.toSourceRange(), it.toDestinationRange()) } }

    println(seeds)

    // Convert the seeds to ranges
    val seedRanges = seeds.windowed(2, 2, false).map {
      it.first() until (it.first() + it.last())
    }
    println(seedRanges)

    val remapped = mappers.fold(seedRanges) { rangesToRemap, mapper ->
      rangesToRemap.flatMap { it.remap(mapper) }
    }

    println(remapped)

    return

    // map from the seedRanges to the first mapper source ranges
    val result = seedRanges.map { seedRange ->
      println("---------")
      seedRange.remap(mappers.first()).also { println(it.joinToString("\n")) }
      //
      // val sr = r1.map { it.source }.map {
      //   val answer = seedRange.splitBy(it)
      //   println("$seedRange split by $it is $answer")
      //   answer
      // }
      // println(sr.joinToString("\n"))
      // sr
    }

    println("======================")

    println(result.joinToString("\n"))

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
    // val maps = progression.reversed().map { rangeMaps.getValue(it) }
    //
    // val goal = 0
    //
    // val closest = seeds.minOf {seed ->
    //   // print("$seed -> ")
    //   val location = maps.fold(seed) { value, map ->
    //     remap(map, value) // .also { print("$it, ") }
    //   }
    //   // println()
    //   location
    // }
    //
    // println(closest)
  }

  private fun showSplit(
    rangeToSplit: LongRange,
    splittingRange: LongRange,
  ) {
    val newRanges = rangeToSplit.splitBy(splittingRange)
    println("$rangeToSplit split by $splittingRange => $newRanges")
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

  companion object {
    val cameFrom = mutableMapOf<LongRange, LongRange>()
  }
}

private fun LongRange.remap(mappers: List<Day.MapperRanges>): List<LongRange> {
  val remapped = mutableListOf<LongRange>()
  val unmapped = ArrayDeque<LongRange>(100)

  unmapped.add(this)

  val mapperIter = mappers.iterator()

  while (unmapped.isNotEmpty() && mapperIter.hasNext()) {
    val range = unmapped.removeFirst()
    val mapper = mapperIter.next()

    if (range.last < mapper.source.first || range.first > mapper.source.last) {
      // total miss
      unmapped.add(range)
    } else {
      // Unmapped before
      if (range.first < mapper.source.first)
        unmapped.add(range.first until mapper.source.first)
      // Unmapped after
      if (range.last > mapper.source.last)
        unmapped.add((mapper.source.last + 1) .. range.last)

      // Now remap the overlapping parts
      val start = max(range.first, mapper.source.first)
      val end = min(range.last, mapper.source.last)

      val newRange = mapper.remapValue(start)..mapper.remapValue(end)
      remapped.add(newRange)
      Day.cameFrom[newRange] = range
    }
  }

  remapped.addAll(unmapped)

  return remapped.toList()
}

private fun LongRange.splitBy(other: LongRange): MutableList<LongRange> {
  val result = mutableListOf<LongRange>()
  if (this.last < other.first) {
    result.add(this)
  }

  if (this.first > other.last) {
    result.add(this)
  }

  if (this.first <= other.first) {
    if (this.last in other) {
      result.add(this.first until other.first)
      result.add(other.first .. this.last)
    }
  }

  if (this.first in other && this.last in other) {
    result.add(this)
  }

  if (this.first in other && this.last > other.last) {
    result.add(first..other.last)
    result.add((other.last + 1)..last)
  }

  return result
}
