package aoc2023.day14

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Direction
import utils.InputNew
import utils.NewGrid
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private lateinit var grid: NewGrid<Char>
  val cycle = listOf(Direction.NORTH, Direction.WEST, Direction.SOUTH, Direction.EAST)
  var stepCounter by mutableStateOf(0)
  var direction = derivedStateOf { cycle[stepCounter % cycle.size] }

  var width by mutableStateOf(0)
  var height by mutableStateOf(0)
  val data = mutableListOf<Char>()

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    O....#....
    O.OO#....#
    .....##...
    OO.#O....O
    .O.....O#.
    O.#..O.#.#
    ..O..#O..O
    .......O..
    #....###..
    #OO..#....
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  init {
    scope.launch {
      initialize()
      grid = NewGrid.fromCollectionOfStrings(input)
      width = grid.width
      height = grid.height
      data.clear()
      data.addAll(grid.data)
    }
  }

  fun part1() {
    val grid = NewGrid.fromCollectionOfStrings(input)
    grid.tilt(Direction.NORTH)
    val answer = northLoad(grid)
    println(answer)
  }

  private fun northLoad(grid: NewGrid<Char>) = grid.mapRowIndexed { i, chars ->
    chars.count { it == 'O' } * (grid.height - i)
  }.sum()

  fun part2() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val loads = (0 until 100).map {
      cycle.forEach { direction ->
        grid.tilt(direction)
      }
      northLoad(grid)
    }

    println(loads.withIndex().joinToString("\n") {(i,v) -> "$i: $v"})

    // println(grid)
  }


  fun execute() {
    job?.cancel()
    job = scope.launch {
      running = true
      while (true) {
        step()
        delay(delayTime)
      }
      running = false
    }
  }

  fun step() {
    grid.tilt(direction.value)
    data.clear()
    data.addAll(grid.data)
    stepCounter += 1
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

private fun NewGrid<Char>.tilt(direction: Direction) {

  when (direction) {
    Direction.NORTH -> {
      this.replaceColumnsIndexed { _, chars ->
        buildString { chars.forEach { c -> append(c) }}.tiltLeft().toList()
      }
    }
    Direction.WEST -> {
      this.replaceRowsIndexed { _, chars ->
        buildString { chars.forEach { c -> append(c) }}.tiltLeft().toList()
      }
    }
    Direction.SOUTH -> {
      this.replaceColumnsIndexed { _, chars ->
        buildString { chars.forEach { c -> append(c) }}.tiltRight().toList()
      }
    }
    Direction.EAST -> {
      this.replaceRowsIndexed { _, chars ->
        buildString { chars.forEach { c -> append(c) }}.tiltRight().toList()
      }
    }
  }
}

private fun String.tiltLeft(): String {
  return this.split('#').map { s ->
    s.partition { it == 'O' }.let { (a, b) -> "$a$b" }
  }.joinToString("#")
}

private fun String.tiltRight(): String {
  return reversed().tiltLeft().reversed()
}
