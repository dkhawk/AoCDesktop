package aoc2023.day03

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.COLORS
import utils.InputNew
import utils.NewGrid
import utils.Vector
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
    467..114..
    ...*......
    ..35..633.
    ......#...
    617*......
    .....+.58.
    ..592.....
    ......755.
    ...${'$'}.*....
    .664.598..
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

  data class LocatedNumber(val value: Int, val row: Int, val start: Int, val end: Int) {
    fun getLocations() = (start..end).map { Vector(it, row) }
  }

  fun part1() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val digitsNearSymbols = grid.findAll { location, c ->
      c.isDigit() && grid.anyNeighbor8(location) { it != '.' && !it.isDigit()  }
    }

    val locationsNearSymbols = digitsNearSymbols.map { it.first }.toSet()

    val locatedNumbers = locateNumbers(grid)

    val engineParts = locatedNumbers.filter { locatedNumber ->
      locatedNumber.getLocations().any { it in locationsNearSymbols }
    }

    val enginePartNumbers = engineParts.map { it.value }

    println(enginePartNumbers.sum())
  }

  fun part2() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val stars = grid.findAll { _, c -> c == '*' }

    val starLocations = stars.map { it.first }

    val locatedNumbers = locateNumbers(grid)

    val locationToNumber = locatedNumbers.flatMap { locatedNumber ->
      locatedNumber.getLocations().map { it to locatedNumber }
    }.toMap()

    val possibleGearRatios = starLocations.map { starLocation ->
      val digits = grid.allNeighbor8(starLocation) { it.isDigit() }
      digits.map { digitLocation -> locationToNumber.getValue(digitLocation.first) }.toSet()
    }

    val gr = possibleGearRatios.filter { it.size == 2 }
      .map { locatedNumber -> locatedNumber.map { it.value.toLong() } }
      .sumOf { gears -> gears.reduce { a, b -> a * b } }

    println(gr)
  }

  private fun locateNumbers(grid: NewGrid<Char>): MutableList<LocatedNumber> {
    fun createLocatedNumber(
      numberBuilder: StringBuilder,
      end: Int,
      row: Int,
    ): LocatedNumber {
      val numberString = numberBuilder.toString()
      val start = end - numberString.length
      val value = numberString.toInt()
      numberBuilder.clear()
      return LocatedNumber(value, row, start, end - 1)
    }

    val locatedNumbers = mutableListOf<LocatedNumber>()

    grid.forEachRowIndexed { row, chars ->
      // Could skip rows that do not have any digits near symbols...

      // Get the "numbers" in this row
      val numberBuilder = StringBuilder()

      chars.forEachIndexed { col, c ->
        if (c.isDigit()) {
          numberBuilder.append(c)
        } else {
          if (numberBuilder.isNotEmpty()) {
            val locatedNumber = createLocatedNumber(numberBuilder, col, row)
            locatedNumbers.add(locatedNumber)
          }
        }
      }
      if (numberBuilder.isNotEmpty()) {
        val locatedNumber = createLocatedNumber(numberBuilder, chars.size, row)
        locatedNumbers.add(locatedNumber)
      }
    }
    return locatedNumbers
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

private fun <T> NewGrid<T>.allNeighbor8(location: Vector, predicate: (T) -> Boolean): List<Pair<Vector, T>> {
  return this.getValidNeighbors8(location).filter { it.second?.let {predicate(it)} ?: false }
}

private fun <T> NewGrid<T>.anyNeighbor8(location: Vector, predicate: (T) -> Boolean): Boolean {
  return this.getNeighbors8(location).mapNotNull { it.second }.any { predicate(it) }
}
