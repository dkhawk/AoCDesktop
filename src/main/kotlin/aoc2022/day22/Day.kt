package aoc2022.day22

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.Heading
import utils.InputNew
import utils.NewGrid
import utils.Vector

const val day = 22
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  fun initialize() {
    input = if (useRealData) {
      InputNew(year, day).readAsLines(filterBlankLines = false)
    } else {
      val dir = InputNew(year, day).baseDirectory
      val fileName = "$dir/22-sample.txt"
      File(fileName).readLines()
    }
  }

  fun part1() {
    val mapLines = input.takeWhile { it.isNotBlank() }
    val commandLine = input.last()

    val grid = createGrid(mapLines)
    val warpMap = createWarpMap(grid)

    val commands = parseCommands(commandLine)

    var location = findStart(grid)
    var heading = Heading.EAST

    commands.forEach { (distance, rotation) ->
      var remaining = distance
      while (remaining > 0) {
        var newLoc = location.advance(heading)
        var c = grid[newLoc]!!
        if (c == ' ') {
          newLoc = warpMap[newLoc to heading]!!
          c = grid[newLoc]!!
        }

        if (c == '#') {
          remaining = 0
        } else {
          location = newLoc
          remaining -= 1
        }
      }

      heading = when (rotation) {
        'R' -> heading.turnRight()
        'L' -> heading.turnLeft()
        else -> heading
      }
    }

    println(location)
    println(heading)
    // Facing is 0 for right (>), 1 for down (v), 2 for left (<), and 3 for up (^)
    val headingScore = when (heading) {
      Heading.EAST -> 0
      Heading.SOUTH -> 1
      Heading.WEST -> 2
      Heading.NORTH -> 3
    }

    val password = (location.y * 1000) + (location.x * 4) + headingScore
    println(password)
  }

  private fun findStart(grid: NewGrid<Char>): Vector {
    return grid.find { loc, c -> c == '.' }!!.first
  }

  data class Command(
    val distance: Int,
    val rotation: Char
  )

  private fun parseCommands(commandLine: String): MutableList<Command> {
    val iter = commandLine.iterator()
    val number = mutableListOf<Char>()
    val commands = mutableListOf<Command>()
    while (iter.hasNext()) {
      val c = iter.next()
      when {
        c == 'R' -> {
          commands.add(Command(number.joinToString("").toInt(), c))
          number.clear()
        }
        c == 'L' -> {
          commands.add(Command(number.joinToString("").toInt(), c))
          number.clear()
        }
        c.isDigit() -> number.add(c)
        else -> {}
      }
    }

    commands.add(Command(number.joinToString("").toInt(), '.'))

    return commands
  }

  private fun createWarpMap(grid: NewGrid<Char>): MutableMap<Pair<Vector, Heading>, Vector> {
    // Create warp map
    val warpMap = mutableMapOf<Pair<Vector, Heading>, Vector>()

    grid.forEachRowIndexed { row, chars ->
      // Skip blank rows
      if (chars.all { it.isWhitespace() }) {
        return@forEachRowIndexed
      }

      val first = chars.indexOfFirst { !it.isWhitespace() }
      val last = chars.indexOfLast { !it.isWhitespace() }

      warpMap[Vector(first - 1, row) to Heading.WEST] = Vector(last, row)
      warpMap[Vector(last + 1, row) to Heading.EAST] = Vector(first, row)
    }

    grid.forEachColumnIndexed { col, chars ->
      // Skip blank rows
      if (chars.all { it.isWhitespace() }) {
        return@forEachColumnIndexed
      }

      val first = chars.indexOfFirst { !it.isWhitespace() }
      val last = chars.indexOfLast { !it.isWhitespace() }

      warpMap[Vector(col, first - 1) to Heading.NORTH] = Vector(col, last)
      warpMap[Vector(col, last + 1) to Heading.SOUTH] = Vector(col, first)
    }
    return warpMap
  }

  private fun createGrid(mapLines: List<String>): NewGrid<Char> {
    val width = mapLines.maxByOrNull { it.length }!!.length

    val fullLines = mapLines.map {
      it.padEnd(width, ' ')
    }.toMutableList()

    // Add one character of extra padding around the entire grid
    fullLines.add(0, "".padStart(width))
    fullLines.add(fullLines.first())


    val paddedGrid = fullLines.map { line ->
      " $line "
    }

    return NewGrid(
      width = width + 2,
      height = paddedGrid.size,
      paddedGrid.joinToString("").toList()
    )
  }

  fun part2() {
    val mapLines = input.takeWhile { it.isNotBlank() }
    val commandLine = input.last()

    val grid = createGrid(mapLines)
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
