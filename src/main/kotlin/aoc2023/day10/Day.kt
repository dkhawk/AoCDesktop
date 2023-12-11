package aoc2023.day10

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.min
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.COLORS
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
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  private var sampleNumber = 0

  private val sampleInputs = listOf(
    """
      .....
      .S-7.
      .|.|.
      .L-J.
      .....
      """,
    """
      ..F7.
      .FJ|.
      SJ.L7
      |F--J
      LJ...
    """,
    """
      ...........
      .S-------7.
      .|F-----7|.
      .||.....||.
      .||.....||.
      .|L-7.F-J|.
      .|..|.|..|.
      .L--J.L--J.
      ...........
    """,
    """
      ..........
      .S------7.
      .|F----7|.
      .||....||.
      .||....||.
      .|L-7F-J|.
      .|..||..|.
      .L--JL--J.
      ..........
    """,
    """
      .F----7F7F7F7F-7....
      .|F--7||||||||FJ....
      .||.FJ||||||||L7....
      FJL7L7LJLJ||LJ.L-7..
      L--J.L7...LJS7F-7L7.
      ....F-J..F7FJ|L7L7L7
      ....L7.F7||L7|.L7L7|
      .....|FJLJ|FJ|F7|.LJ
      ....FJL-7.||.||||...
      ....L---J.LJ.LJLJ...
    """,
    """
      FF7FSF7F7F7F7F7F---7
      L|LJ||||||||||||F--J
      FL-7LJLJ||||||LJL-77
      F--JF--7||LJLJ7F7FJ-
      L---JF-JLJ.||-FJLJJ7
      |F|F-JF---7F7-L7L|7|
      |FFJF7L7F-JF7|JL---7
      7-L-JL7||F7|L7F-7F7|
      L.L7LFJ|||||FJL7||LJ
      L7JLJL-JLJLJL--JLJ.L
    """
  ).map { it.trimIndent().split("\n") }

  fun initialize() {
    loadInput()
  }

  private fun loadInput() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInputs[sampleNumber]
    }
  }

  fun part1() {
    /*
    | is a vertical pipe connecting north and south.
- is a horizontal pipe connecting east and west.
L is a 90-degree bend connecting north and east.
J is a 90-degree bend connecting north and west.
7 is a 90-degree bend connecting south and west.
F is a 90-degree bend connecting south and east.
. is ground; there is no pipe in this tile.
S is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.
     */

    sampleNumber = 1
    loadInput()

    val grid = NewGrid.fromCollectionOfStrings(input)

    val (startLocation, startTile) = grid.find { _, c -> c == 'S' }
      ?: throw Exception("Failed to find the start!")

    var location = startLocation
    var tile = startTile

    var lastLocation = startLocation

    var count = 0
    do {
      val validDirections = when (tile) {
        'S' -> Heading.values().toList()
        '|' -> listOf(Heading.NORTH, Heading.SOUTH)
        '-' -> listOf(Heading.EAST, Heading.WEST)
        'L' -> listOf(Heading.EAST, Heading.NORTH)
        'J' -> listOf(Heading.NORTH, Heading.WEST)
        '7' -> listOf(Heading.SOUTH, Heading.WEST)
        'F' -> listOf(Heading.EAST, Heading.SOUTH)
        else -> throw Exception("Invalid tile: $tile")
      }

      var neighbors = grid.getValidNeighbors(location)

      neighbors = neighbors.filterNot { it.first == lastLocation }
      neighbors = neighbors.filterNot { it.second == '.' }
      neighbors = neighbors.filter { location.headingTo(it.first) in validDirections }

      val valid = neighbors.filter { (next, nextChar) ->
        when (location.directionTo(next).toHeading()) {
          Heading.NORTH -> nextChar in listOf('7', 'F', '|', 'S')
          Heading.SOUTH -> nextChar in listOf('L', 'J', '|', 'S')
          Heading.EAST -> nextChar in listOf('7', 'J', '-', 'S')
          Heading.WEST -> nextChar in listOf('L', 'F', '-', 'S')
        }
      }

      if (valid.size != 1 && count != 0) {
        throw Exception("Too many options!")
      }

      lastLocation = location
      location = valid.first().first
      tile = valid.first().second

      count += 1
    } while (location != startLocation)

    println(count)
  }

  fun part2() {
    /*
    | is a vertical pipe connecting north and south.
- is a horizontal pipe connecting east and west.
L is a 90-degree bend connecting north and east.
J is a 90-degree bend connecting north and west.
7 is a 90-degree bend connecting south and west.
F is a 90-degree bend connecting south and east.
. is ground; there is no pipe in this tile.
S is the starting position of the animal; there is a pipe on this tile, but your sketch doesn't show what shape the pipe has.
     */

    sampleNumber = 5
    loadInput()

    val grid = NewGrid.fromCollectionOfStrings(input)

    val (startLocation, startTile) = grid.find { _, c -> c == 'S' }
      ?: throw Exception("Failed to find the start!")

    var location = startLocation
    var tile = startTile

    var lastLocation = startLocation

    val loopTiles = mutableSetOf<Vector>()

    var count = 0
    do {
      val validDirections = when (tile) {
        'S' -> Heading.values().toList()
        '|' -> listOf(Heading.NORTH, Heading.SOUTH)
        '-' -> listOf(Heading.EAST, Heading.WEST)
        'L' -> listOf(Heading.EAST, Heading.NORTH)
        'J' -> listOf(Heading.NORTH, Heading.WEST)
        '7' -> listOf(Heading.SOUTH, Heading.WEST)
        'F' -> listOf(Heading.EAST, Heading.SOUTH)
        else -> throw Exception("Invalid tile: $tile")
      }

      loopTiles.add(location)

      var neighbors = grid.getValidNeighbors(location)

      neighbors = neighbors.filterNot { it.first == lastLocation }
      neighbors = neighbors.filterNot { it.second == '.' }
      neighbors = neighbors.filter { location.headingTo(it.first) in validDirections }

      val valid = neighbors.filter { (next, nextChar) ->
        when (location.directionTo(next).toHeading()) {
          Heading.NORTH -> nextChar in listOf('7', 'F', '|', 'S')
          Heading.SOUTH -> nextChar in listOf('L', 'J', '|', 'S')
          Heading.EAST -> nextChar in listOf('7', 'J', '-', 'S')
          Heading.WEST -> nextChar in listOf('L', 'F', '-', 'S')
        }
      }

      if (valid.size != 1 && count != 0) {
        throw Exception("Too many options!")
      }

      lastLocation = location
      location = valid.first().first
      tile = valid.first().second

      count += 1
    } while (location != startLocation)

    // println(
    //   grid.toStringWithHighlights(
    //     COLORS.LT_RED.toString() to { _, location -> location in loopTiles }
    //   )
    // )

    val internalSpaces = mutableSetOf<Vector>()

    // TODO Replace 'S' with its shape!
    val startNeighbors = grid.getValidNeighbors(startLocation).filter { (next, nextChar) ->
      when (location.directionTo(next).toHeading()) {
        Heading.NORTH -> nextChar in listOf('7', 'F', '|', 'S')
        Heading.SOUTH -> nextChar in listOf('L', 'J', '|', 'S')
        Heading.EAST -> nextChar in listOf('7', 'J', '-', 'S')
        Heading.WEST -> nextChar in listOf('L', 'F', '-', 'S')
      }
    }.map { startLocation.headingTo(it.first) }.sorted()

    // println(startNeighbors)

    val replacement = when (startNeighbors) {
      listOf(Heading.NORTH, Heading.SOUTH).sorted() -> '|'
      listOf(Heading.EAST, Heading.WEST).sorted() -> '-'
      listOf(Heading.EAST, Heading.NORTH).sorted() -> 'L'
      listOf(Heading.NORTH, Heading.WEST).sorted() -> 'L'
      listOf(Heading.SOUTH, Heading.WEST).sorted() -> '7'
      listOf(Heading.EAST, Heading.SOUTH).sorted() -> 'F'
      else -> throw Exception("invalid starting neighbors ")
    }

    // println(replacement)

    grid[startLocation] = replacement

    // println(grid.toStringWithHighlights { _, loc -> loc == startLocation })

    // convert non-loop tiles

    val nonLoopTiles = grid.findAll { vector, c -> vector !in loopTiles }.map { it.first }
    nonLoopTiles.forEach { l -> grid[l] = '.' }

    val turnCharacters = listOf('F', '7', 'L', 'J')

    grid.mapRowIndexed { row, data ->
      var numCrossings = 0
      var numTurns = 0
      val turns = turnCharacters.associateWith { 0 }.toMutableMap()
      data.forEachIndexed { index, c ->
        if (c == '|') {
          numCrossings += 1
        }

        if (c in turns) {
          turns[c] = 1 + turns.getOrDefault(c, 0)
        }

        if (c == '.') {
          if ((numCrossings + effectiveCrossings(turns)).isOdd()) {
            val loc = Vector(index, row)
            grid[loc] = 'I'
            internalSpaces.add(loc)
          }
        }
      }
    }

    // println(
    //   grid.toStringWithHighlights(
    //     COLORS.LT_RED.toString() to { _, location -> location in loopTiles },
    //     COLORS.LT_GREEN.toString() to { _, location -> location in internalSpaces }
    //   )
    // )

    println(internalSpaces.size)
  }

  private fun effectiveCrossings(turns: Map<Char, Int>): Int {
    // Count complements
    // L to 7
    val ls = turns.getOrDefault('L', 0)
    val sevens = turns.getOrDefault('7', 0)
    val downs = min(ls, sevens)

    val fs = turns.getOrDefault('F', 0)
    val js = turns.getOrDefault('J', 0)
    val ups = min(fs, js)

    return downs + ups
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

private fun Int.isOdd() = (this and 1) == 1
