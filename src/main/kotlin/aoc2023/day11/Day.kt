package aoc2023.day11

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.lang.Integer.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import utils.InputNew
import utils.NewGrid
import utils.Vector
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    ...#......
    .......#..
    #.........
    ..........
    ......#...
    .#........
    .........#
    ..........
    .......#..
    #...#.....
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

  fun part1() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val emptyColumns = grid.filteredColumnsIndexed { i, chars -> !chars.contains('#') }.map { it.first }
    emptyColumns.reversed().forEach { colIndex ->
      grid.addColumn(colIndex, '.')
    }

    val emptyRows = grid.filteredRowsIndexed { i, chars -> !chars.contains('#') }.map { it.first }
    emptyRows.reversed().forEach { rowIndex ->
      grid.addRow(rowIndex, '.')
    }

    val galaxies = grid.findAll { _, c -> c == '#' }.mapIndexed { index, pair -> (index + 1) to pair }

    val combinations = sequence {
      galaxies.dropLast(1).forEachIndexed { index, g1 ->
        galaxies.subList(index + 1, galaxies.size).forEach { g2 ->
          yield(g1 to g2)
        }
      }
    }

    val distances = combinations.map { (a, b) ->
      (a.first to b.first) to a.second.first.cityDistanceTo(b.second.first)
    }

    println(distances.sumOf { it.second })
  }

  fun part2() {
    val grid = NewGrid.fromCollectionOfStrings(input)
    val magnitude = 1_000_000L

    val emptyColumns = grid.filteredColumnsIndexed { i, chars -> !chars.contains('#') }.map { it.first }
    emptyColumns.reversed().forEach { colIndex ->
      grid.replaceColumn(colIndex, '+')
    }

    val emptyRows = grid.filteredRowsIndexed { i, chars -> !chars.contains('#') }.map { it.first }
    emptyRows.reversed().forEach { rowIndex ->
      grid.replaceRow(rowIndex, '+')
    }

    println(grid)

    val galaxies = grid.findAll { _, c -> c == '#' }.mapIndexed { index, pair -> (index + 1) to pair }

    val combinations = sequence {
      galaxies.dropLast(1).forEachIndexed { index, g1 ->
        galaxies.subList(index + 1, galaxies.size).forEach { g2 ->
          yield(g1 to g2)
        }
      }
    }

    val distances = combinations.map { (a, b) ->
      (a.first to b.first) to grid.cityPathTo(a.second.first, b.second.first)
    }

    val answer = distances.sumOf { (_, path) ->
      path.sumOf { c ->
        if (c == '+') {
          magnitude
        } else {
          1
        }
      }
    }

    println(answer)
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

private fun <T> NewGrid<T>.cityPathTo(start: Vector, end: Vector): List<T> {
  return buildList<T> {
    val directions = start.directionTo(end).sign
    val xRange = if (directions.x > 0) start.x .. end.x else end.x ..start.x
    val yRange = if (directions.y > 0) start.y .. end.y else end.y .. start.y

    xRange.drop(1).forEach { x ->
      add(get(x, start.y)!!)
    }

    yRange.drop(1).forEach { y ->
      add(get(end.x, y)!!)
    }
  }
}
