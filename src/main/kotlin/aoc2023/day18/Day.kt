package aoc2023.day18

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.Heading
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
    R 6 (#70c710)
    D 5 (#0dc571)
    L 2 (#5713f0)
    D 2 (#d2c081)
    R 2 (#59c680)
    D 2 (#411b91)
    L 5 (#8ceee2)
    U 2 (#caa173)
    L 1 (#1b58a2)
    U 2 (#caa171)
    R 2 (#7807d2)
    U 3 (#a77fa3)
    L 2 (#015232)
    U 2 (#7a21e3)
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

  enum class DigDirection {
    UP,
    DOWN,
    LEFT,
    RIGHT,
  }

  data class DigStep(val direction: Heading, val distance: Int, val color: String)

  data class Step(val direction: Heading, val distance: Int)

  fun parse(line: String): DigStep {
    val parts = line.split(" ")
    val direction = parts[0].first().toHeading()
    val distance = parts[1].toInt()
    val color = parts[2].substringAfter('#').dropLast(1)
    return DigStep(direction, distance, color)
  }

  fun part1() {
    val steps = input.map { parse(it) }
    // println(steps.joinToString("\n"))

    var location = Vector(0, 0)
    var maxX = Int.MIN_VALUE
    var maxY = Int.MIN_VALUE

    var minX = Int.MAX_VALUE
    var minY = Int.MAX_VALUE

    val path: List<Vector> = buildList {
      add(location)
      steps.forEach { digStep ->
        location += (digStep.direction.vector * digStep.distance)
        add(location)
        maxX = maxX.coerceAtLeast(location.x)
        maxY = maxY.coerceAtLeast(location.y)

        minX = minX.coerceAtMost(location.x)
        minY = minY.coerceAtMost(location.y)
      }
    }

    val min = Vector(minX, minY)
    val max = Vector(maxX, maxY)

    // Add a border around the path, plus one since the width/height are exclusive
    val delta = (max - min) + Vector(3, 3)

    val grid = NewGrid(delta.x, delta.y, '.')

    // Again, add one to give a border around the edge
    val offset = Vector(1, 1) - min

    path.windowed(2, 1).forEach { (start, end) ->
      val direction = start.headingTo(end)
      var current = start

      while (current != end) {
        grid[current + offset] = '#'
        current = current.advance(direction)
      }
    }

    // println()
    // println(grid)

    grid.floodFill(Vector(0, 0), 'x')

    // println()
    // println(grid)

    val volume = grid.data.count { it == '#' || it == '.' }
    println(volume)
  }

  sealed class GridPart {
    data class Line(val index: Int): GridPart()
    data class GridRange(val range: IntRange): GridPart()
  }

  fun part2() {
    val steps = input.map { parse(it) }.map { it.color.toStep() }

    var location = Vector(0, 0)
    var maxX = Int.MIN_VALUE
    var maxY = Int.MIN_VALUE

    var minX = Int.MAX_VALUE
    var minY = Int.MAX_VALUE

    val path: List<Vector> = buildList {
      add(location)
      steps.forEach { digStep ->
        location += (digStep.direction.vector * digStep.distance)
        add(location)
        maxX = maxX.coerceAtLeast(location.x)
        maxY = maxY.coerceAtLeast(location.y)

        minX = minX.coerceAtMost(location.x)
        minY = minY.coerceAtMost(location.y)
      }
    }

    val min = Vector(minX, minY)
    val max = Vector(maxX, maxY)

    println(min)
    println(max)

    println("path")
    println(path.joinToString("\n"))

    println()
    val xs = path.map { it.x }.toSortedSet()
    val ys = path.map { it.y }.toSortedSet()

    println("x")
    println(xs.joinToString("\n"))

    println()
    println("y")
    println(ys.joinToString("\n"))

    val xGridParts = buildList {
      xs.windowed(2, 1).forEach {
        add(GridPart.Line(it.first()))
        if (it.first() != it.last()) {
          add(GridPart.GridRange((it.first() + 1) until it.last()))
        }
        add(GridPart.Line(it.last()))
      }
    }

    val yGridParts = buildList {
      ys.windowed(2, 1).forEach {
        add(GridPart.Line(it.first()))
        if (it.first() != it.last()) {
          add(GridPart.GridRange((it.first() + 1) until it.last()))
        }
        add(GridPart.Line(it.last()))
      }
    }

    val width = xGridParts.size + 3 // Borders + exclusive
    val height = yGridParts.size + 3 // Borders + exclusive



    println()
    println(xGridParts.joinToString("\n"))
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

private fun String.toStep(): Day.Step {
  val distance = subSequence(0, 5).toString().toInt(16)
  val heading = when (last()) {
    '0' -> Heading.EAST
    '1' -> Heading.SOUTH
    '2' -> Heading.WEST
    '3' -> Heading.NORTH
    else -> throw Exception("Illegal direction number: ${last()}")
  }

  return Day.Step(heading, distance)
}

private fun <T> NewGrid<T>.floodFill(start: Vector, char: T) {
  val heads = ArrayDeque<Vector>()
  heads.add(start)
  val target = get(start)

  while (heads.isNotEmpty()) {
    val loc = heads.removeFirst()
    getValidNeighbors(loc).filter { (l, c) -> c == target }.forEach { (l, c) ->
      heads.addLast(l)
      this[l] = char
    }
  }
}

private fun Char.toHeading(): Heading {
  return when (this) {
    'U' -> Heading.NORTH
    'D' -> Heading.SOUTH
    'L' -> Heading.WEST
    'R' -> Heading.EAST
    else -> throw Exception("Invalid direction: $this")
  }
}

private fun Char.toDigDirection(): Day.DigDirection {
  return when (this) {
    'U' -> Day.DigDirection.UP
    'D' -> Day.DigDirection.DOWN
    'L' -> Day.DigDirection.LEFT
    'R' -> Day.DigDirection.RIGHT
    else -> throw Exception("Invalid direction: $this")
  }
}
