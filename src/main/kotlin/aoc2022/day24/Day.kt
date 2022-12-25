package aoc2022.day24

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.PriorityQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid
import utils.Vector
import utils.Vector3d

const val day = 24
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  val sampleInput = """
    #.######
    #>>.<^<#
    #.<..<<#
    #>v.><>#
    #<^v^^>#
    ######.#
  """.trimIndent().split("\n")

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }.filter { it.isNotBlank() }
  }

  fun part1() {
    val grid = NewGrid(input.first().length, input.size, input.joinToString("").toList())
    val start = Vector(input.first().indexOf('.'), 0)
    val goal = Vector(input.last().lastIndexOf('.'), input.lastIndex)

    val grids = mutableListOf<NewGrid<Char>>()
    val horizontalPatterns = getHorizontalPatterns(grid)
    val verticalPatterns = getVerticalPatterns(grid)

    val cycle = lcm(grid.width - 2, grid.height - 2)

    val grid3d =
      createGridStack(cycle, grids, grid, horizontalPatterns, verticalPatterns, start, goal)

    // println(grid3d)

    val start3d = Vector3d(start.x, start.y, 0)
    val goal3d = Vector3d(goal.x, goal.y, 0)

    println(start3d)
    println(goal3d)

    // val path = findPath(grid3d, start3d, goal3d)
    // println(path)
    // println(path.size)

    val path = dfs(grid3d, start3d, goal3d)
    println(path)
    println(path.lastIndex)

    // printMapWithPath(grid3d, path)
  }

  fun part2() {
    val grid = NewGrid(input.first().length, input.size, input.joinToString("").toList())
    val start = Vector(input.first().indexOf('.'), 0)
    val goal = Vector(input.last().lastIndexOf('.'), input.lastIndex)

    val grids = mutableListOf<NewGrid<Char>>()
    val horizontalPatterns = getHorizontalPatterns(grid)
    val verticalPatterns = getVerticalPatterns(grid)

    val cycle = lcm(grid.width - 2, grid.height - 2)

    val grid3d =
      createGridStack(cycle, grids, grid, horizontalPatterns, verticalPatterns, start, goal)

    // println(grid3d)

    val start3d = Vector3d(start.x, start.y, 0)
    val goal3d = Vector3d(goal.x, goal.y, 0)

    println(start3d)
    println(goal3d)

    // val path = findPath(grid3d, start3d, goal3d)
    // println(path)
    // println(path.size)

    val path0 = dfs(grid3d, start3d, goal3d)
    // println(path0)
    println(path0.lastIndex)

    val path1 = dfs(grid3d, path0.last(), start3d)
    // println(path1)
    println(path1.lastIndex)

    val path2 = dfs(grid3d, path1.last(), goal3d)
    // println(path1)
    println(path2.lastIndex)

    val total = path0.lastIndex + path1.lastIndex + path2.lastIndex
    println(total)
  }

  private fun createGridStack(
    cycle: Int,
    grids: MutableList<NewGrid<Char>>,
    grid: NewGrid<Char>,
    horizontalPatterns: List<Pair<List<Int>, List<Int>>?>,
    verticalPatterns: List<Pair<List<Int>, List<Int>>?>,
    start: Vector,
    goal: Vector,
  ): Grid3d {
    repeat(cycle) {
      grids.add(
        createGrid(
          it,
          grid.width,
          grid.height,
          horizontalPatterns,
          verticalPatterns,
          start,
          goal
        )
      )
    }

    val grid3d = Grid3d(grid.width, grid.height, cycle)
    val t = grids.flatMap { it.data }
    grid3d.setData(t.toCharArray())
    return grid3d
  }

  private fun getVerticalPatterns(grid: NewGrid<Char>): List<Pair<List<Int>, List<Int>>?> {
    val verticalPatterns = grid.mapColumnIndexed { col, line ->
      if (col == 0 || col == grid.width - 1) {
        null
      } else {
        line.mapIndexedNotNull { index, c -> if (c == 'v') index - 1 else null } to
          line.mapIndexedNotNull { index, c -> if (c == '^') index - 1 else null }
      }
    }
    return verticalPatterns
  }

  private fun getHorizontalPatterns(grid: NewGrid<Char>): List<Pair<List<Int>, List<Int>>?> {
    val horizontalPatterns = grid.mapRowIndexed { row, line ->
      if (row == 0 || row == grid.height - 1) {
        null
      } else {
        line.mapIndexedNotNull { index, c -> if (c == '>') index - 1 else null } to
          line.mapIndexedNotNull { index, c -> if (c == '<') index - 1 else null }
      }
    }
    return horizontalPatterns
  }

  private fun printMapWithPath(grid3d: Grid3d, path: List<Vector3d>) {
    path.forEachIndexed { minute, location ->
      println("Minute $minute")
      (0 until grid3d.height).forEach { y ->
        val row = grid3d.getRow(y, location.z % grid3d.depth).joinToString("") { if (it == '.') " " else "$it" }
        val rowWithPosition = row.mapIndexed { x, c ->
          if (location.x == x && location.y == y) {
            "@"
          } else {
            c
          }
        }.joinToString("")

        println("$row     $rowWithPosition")
      }
      println()
    }
  }

  private fun dfs(grid3d: Grid3d, start: Vector3d, goal: Vector3d): List<Vector3d> {
    val queue = ArrayDeque<Vector3d>()
    queue.addLast((start))

    val scores = mutableMapOf(start to 0)
    val cameFrom = mutableMapOf<Vector3d, Vector3d>()

    var finish: Vector3d? = null

    while (queue.isNotEmpty() && finish == null) {
      val current = queue.removeFirst()
      val neighbors = current.getNeighborsAbove()
      // println(current)
      // println(neighbors)
      val filteredNeighbors = neighbors.filter {
        it.x in 0 until grid3d.width &&
          it.y in 0 until grid3d.height
      }.filter { grid3d.getInfiniteZ(it) == '.' }
      // println(filteredNeighbors)

      val score = scores.getValue(current) + 1

      filteredNeighbors.forEach { neighbor ->

        if (neighbor.x == goal.x && neighbor.y == goal.y) {
          cameFrom[neighbor] = current
          scores[neighbor] = score
          finish = neighbor
          return@forEach
        }

        val ns = scores[neighbor] ?: Int.MAX_VALUE
        if (score < ns) {
          cameFrom[neighbor] = current
          scores[neighbor] = score
          if (neighbor !in queue) {
            queue.add(neighbor)
          }
        }
      }
    }

    if (finish == null) {
      return emptyList<Vector3d>()
    }

    var current = finish!!
    val path = mutableListOf<Vector3d>()
    path.add(current)

    while (current != start) {
      current = cameFrom.getValue(current)
      path.add(current)
    }

    return path.reversed()
  }

  private fun findPath(grid3d: Grid3d, start: Vector3d, goal: Vector3d): List<Vector3d> {
    // It is okay for this to be not quite right as it is a heuristic
    fun costHeuristic(v: Vector3d): Double = v.distance(goal.copy(z = v.z))

    val cameFrom = mutableMapOf<Vector3d, Vector3d>()

    val gScore = mutableMapOf<Vector3d, Double>().withDefault { 10_000_000.0 }
    gScore[start] = 0.0

    val fScoreMap = mutableMapOf<Vector3d, Double>().withDefault { 10_000_000.0 }
    fScoreMap[start] = costHeuristic(start)

    val openSet = PriorityQueue<Vector3d>(500, compareBy { fScoreMap[it] })
    openSet.add(start)

    var success = false

    while (openSet.isNotEmpty()) {
      val current = openSet.remove()
      if (current.y == goal.y && current.x == goal.x) {
        success = true
        break
      }

      val neighbors = current.getNeighborsAbove().filter {
        it.x in 0 until grid3d.width &&
        it.y in 0 until grid3d.height
      }

      val emptyNeighbors = neighbors.filter { grid3d.getInfiniteZ(it) == '.' }

      emptyNeighbors
        .forEach { neighbor ->
          val tentativeScore = gScore.getValue(current) + 1.0
          if (tentativeScore < gScore.getValue(neighbor)) {
            cameFrom[neighbor] = current
            gScore[neighbor] = tentativeScore
            val weight = costHeuristic(neighbor)
            fScoreMap[neighbor] = tentativeScore + weight

            if (neighbor !in openSet) {
              openSet.add(neighbor)
            }
          }
        }
    }

    if (success) {
      // reconstruct path here
      var current = goal
      val path = mutableListOf(current)
      while (current in cameFrom) {
        current = cameFrom[current]!!
        path.add(current)
      }
      return path.reversed()
    } else {
      throw Exception("No path found!")
    }

  }

  private fun lcm(i0: Int, i1: Int): Int {
    var m0 = i0
    var m1 = i1

    while (true) {
      if (m0 == m1) {
        return m0
      }

      if (m0 < m1) {
        m0 += i0
      } else {
        m1 += i1
      }
    }
  }

  private fun createGrid(
    iteration: Int,
    width: Int,
    height: Int,
    horizontalPatterns: List<Pair<List<Int>, List<Int>>?>,
    verticalPatterns: List<Pair<List<Int>, List<Int>>?>,
    start: Vector,
    finish: Vector,
  ): NewGrid<Char> {
    val data = CharArray(width * height).map { '.' }.toList()
    val grid = NewGrid(width, height, data)
    (0 until grid.width).forEach { x ->
      grid[x, 0] = '#'
      grid[x, grid.height - 1] = '#'
    }
    (0 until grid.height).forEach { y ->
      grid[0, y] = '#'
      grid[grid.width - 1, y] = '#'
    }
    grid[start] = '.'
    grid[finish] = '.'

    horizontalPatterns.forEachIndexed { y, patterns ->
      if (patterns != null) {
        val (forward, backward) = patterns
        forward.forEach { x0 ->
          val x = (x0 + iteration) % (width - 2) + 1
          updateGrid(grid, x, y, '>')
        }
        backward.forEach { x0 ->
          var x1 = (x0 - iteration) % (width - 2)
          while (x1 < 0) {
            x1 += (width - 2)
          }
          val x = x1 + 1

          updateGrid(grid, x, y, '<')
        }
      }
    }

    verticalPatterns.forEachIndexed { x, patterns ->
      if (patterns != null) {
        val (forward, backward) = patterns
        forward.forEach { y0 ->
          val y = (y0 + iteration) % (height - 2) + 1
          updateGrid(grid, x, y, 'v')
        }
        backward.forEach { y0 ->
          var y1 = (y0 - iteration) % (height - 2)
          while (y1 < 0) {
            y1 += (height - 2)
          }
          val y = y1 + 1
          updateGrid(grid, x, y, '^')
        }
      }
    }

    return grid
  }

  private fun updateGrid(grid: NewGrid<Char>, x0: Int, y: Int, newC: Char) {
    val c = grid[x0, y]!!
    grid[x0, y] = when {
      c == '.' -> newC
      c.isDigit() -> c + 1
      else -> '2'
    }
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

private fun Vector3d.getNeighborsAbove(): List<Vector3d> {
  return Vector(x, y).neighbors().map {
    Vector3d(it.x, it.y, z + 1)
  } + Vector3d(x, y, z + 1)
}

class Grid3d(val width: Int, val height: Int, val depth: Int) {
  val data = CharArray(depth * height * width)
  val area = width * height

  fun setData(input: CharArray) {
    input.copyInto(data)
  }

  override fun toString(): String {
    return data.toList().windowed(area, area).joinToString("\n\n") { subdata ->
      NewGrid(width, height, subdata.joinToString("").toList()).toString()
    }
  }

  fun getInfiniteZ(location: Vector3d): Char {
    val d = location.z % depth
    val index = (area * d) + (width * location.y) + location.x
    return data[index]
  }

  fun getRow(y: Int, z: Int): List<Char> {
    val offset = toIndex(0, y, z)
    return data.asList().subList(offset, offset + width)
  }

  private fun toIndex(x: Int, y: Int, z: Int) = (z * area) + (y * width) + x
  private fun toIndex(location: Vector3d) = toIndex(location.x, location.y, location.z)

  operator fun get(location: Vector3d): Char {
    return data[toIndex(location)]
  }
}
