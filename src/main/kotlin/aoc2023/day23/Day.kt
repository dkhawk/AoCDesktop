package aoc2023.day23

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
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
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    #.#####################
    #.......#########...###
    #######.#########.#.###
    ###.....#.>.>.###.#.###
    ###v#####.#v#.###.#.###
    ###.>...#.#.#.....#...#
    ###v###.#.#.#########.#
    ###...#.#.#.......#...#
    #####.#.#.#######.#.###
    #.....#.#.#.......#...#
    #.#####.#.#.#########v#
    #.#...#...#...###...>.#
    #.#.#v#######v###.###v#
    #...#.>.#...>.>.#.###.#
    #####v#.#.###v#.#.###.#
    #.....#...#...#.#.#...#
    #.#########.###.#.#.###
    #...###...#...#...#.###
    ###.###.#.###v#####v###
    #...#...#.#.>.>.#.>.###
    #.###.###.#.###.#.#v###
    #.....###...###...#...#
    #####################.#
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

    val start = Vector(1, 0)
    // val start = Vector(21, 11)
    val goal = Vector(input.last().lastIndex - 1, input.lastIndex)
    // grid[start] = 'S'
    // grid[goal] = 'G'
    //
    // println(
    //   grid.toStringWithHighlights(
    //     COLORS.RED.toString() to { _, vector -> vector == start },
    //     COLORS.GREEN.toString() to { _, vector -> vector == goal },
    //   )
    // )

    // return

    val visited = mutableSetOf<Vector>()
    val longestPath = findLongestPath(grid, start, goal, visited).dropLast(1).reversed()
    println(longestPath)

    longestPath.forEach { grid[it] = 'O' }
    grid[start] = 'S'

    println(
      grid.toStringWithHighlights(
        COLORS.RED.toString() to { _, vector -> vector == start },
        COLORS.BLUE.toString() to { _, vector -> vector == goal },
        COLORS.GREEN.toString() to { c, _ -> c == 'O' },
      )
    )

    println(longestPath.size)
  }

  private val slopes = mapOf(
    '^' to Heading.NORTH,
    'v' to Heading.SOUTH,
    '<' to Heading.WEST,
    '>' to Heading.EAST,
  )

  private val validTiles = slopes.keys + '.'

  private fun findLongestPath(
    grid: NewGrid<Char>,
    location: Vector,
    goal: Vector,
    visited: Set<Vector>
  ): List<Vector> {
    println("$location ${visited.size}")
    if (location == goal) return listOf(goal)

    val forcedHeading = grid[location]?.let { slopes[it] }

    val localVisited = visited + location

    if (forcedHeading != null) {
      val next = location.advance(forcedHeading)
      return if (next in localVisited) {
        emptyList()
      } else {
        findLongestPath(grid, next, goal, localVisited) + location
      }
    }

    val paths = grid.getValidNeighbors(location)
      .filter { (loc, c) ->
        c in validTiles && loc !in visited
      }
      .filter { (loc, c) ->
        location.headingTo(loc) != slopes[c]?.opposite()
      }
      .map { neighbor ->
        findLongestPath(grid, neighbor.first, goal, localVisited) + location
      }

    return paths.maxByOrNull { it.size } ?: emptyList()
  }

  fun part2() {
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
