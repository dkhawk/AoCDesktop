package aoc2023.day13

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: String

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L
  private var sampleNumber = 0

  val sampleInput = """
    #.##..##.
    ..#.##.#.
    ##......#
    ##......#
    ..#.##.#.
    ..##..##.
    #.#.##.#.

    #...##..#
    #....#..#
    ..##..###
    #####.##.
    #####.##.
    ..##..###
    #....#..#
  """.trimIndent()

  fun initialize() {
  }

  private fun loadData() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsString()
      realInput
    } else {
      sampleInput
    }
  }

  enum class ReflectionDirection {
    Horizontal,
    Vertical
  }

  fun part1() {
    loadData()

    val patternGrids = input.split("\n\n").filter { it.isNotEmpty() }.map {
      NewGrid.fromCollectionOfStrings(it.trim().split("\n"))
    }
    val reflectionPoints = patternGrids.map { grid ->
      buildList {
        horizontalReflectionPoint(grid)?.let { hr ->
          add(Pair(ReflectionDirection.Horizontal, hr.first + 1))
        }
        verticalReflectionPoint(grid)?.let {vr ->
          add(Pair(ReflectionDirection.Vertical, vr.first + 1))
        }
      }
    }

    // println(reflectionPoints)

    val answer = reflectionPoints.flatten().sumOf { reflectionPoint ->
      when (reflectionPoint.first) {
        ReflectionDirection.Horizontal -> reflectionPoint.second
        ReflectionDirection.Vertical -> reflectionPoint.second * 100
      }
    }

    println(answer)
  }

  private fun horizontalReflectionPoint(grid: NewGrid<Char>): Pair<Int, Int>? {
    var reflectionPoint: Pair<Int, Int>? = null
    for (leftIndex in 0 until (grid.width - 1)) {
      val rightIndex = leftIndex + 1
      // Now starting from leftIndex and going down
      // And leftIndex + 1 and going up

      var isReflective = true

      var li = leftIndex
      var ri = rightIndex
      while (li >= 0 && ri < grid.width) {
        val leftColumn = grid.getColumn(li)
        val rightColumn = grid.getColumn(ri)
        if (leftColumn != rightColumn) {
          isReflective = false
          break
        }

        li -= 1
        ri += 1
      }

      if (isReflective) {
        reflectionPoint = leftIndex to rightIndex
        break
      }
    }
    return reflectionPoint
  }

  private fun horizontalReflectionPointWithSmudges(grid: NewGrid<Char>): Pair<Int, Int>? {
    var reflectionPoint: Pair<Int, Int>? = null
    for (leftIndex in 0 until (grid.width - 1)) {
      val rightIndex = leftIndex + 1
      // Now starting from leftIndex and going down
      // And leftIndex + 1 and going up

      var isReflective = true

      var li = leftIndex
      var ri = rightIndex
      while (li >= 0 && ri < grid.width) {
        val leftColumn = grid.getColumn(li)
        val rightColumn = grid.getColumn(ri)

        val offByOne = leftColumn.zip(rightColumn).count { (left, right) -> left != right }

        if (leftColumn != rightColumn) {
          isReflective = false
          break
        }

        li -= 1
        ri += 1
      }

      if (isReflective) {
        reflectionPoint = leftIndex to rightIndex
        break
      }
    }
    return reflectionPoint
  }

  private fun verticalReflectionPoint(grid: NewGrid<Char>): Pair<Int, Int>? {
    var reflectionPoint: Pair<Int, Int>? = null
    for (topIndex in 0 until (grid.height - 1)) {
      val bottomIndex = topIndex + 1
      // Now starting from leftIndex and going down
      // And leftIndex + 1 and going up

      var isReflective = true

      var ti = topIndex
      var bi = bottomIndex
      while (ti >= 0 && bi < grid.height) {
        val topRow = grid.getRow(ti)
        val bottomRow = grid.getRow(bi)
        if (topRow != bottomRow) {
          isReflective = false
          break
        }

        ti -= 1
        bi += 1
      }

      if (isReflective) {
        reflectionPoint = topIndex to bottomIndex
        break
      }
    }
    return reflectionPoint
  }

  fun part2() {
    loadData()

    val patternGrids = input.split("\n\n").filter { it.isNotEmpty() }.map {
      NewGrid.fromCollectionOfStrings(it.trim().split("\n"))
    }
    val reflectionPoints = patternGrids.map { grid ->
      buildList {
        horizontalReflectionPoint(grid)?.let { hr ->
          add(Pair(ReflectionDirection.Horizontal, hr.first + 1))
        }
        verticalReflectionPoint(grid)?.let {vr ->
          add(Pair(ReflectionDirection.Vertical, vr.first + 1))
        }
      }
    }

    // println(reflectionPoints)

    val answer = reflectionPoints.flatten().sumOf { reflectionPoint ->
      when (reflectionPoint.first) {
        ReflectionDirection.Horizontal -> reflectionPoint.second
        ReflectionDirection.Vertical -> reflectionPoint.second * 100
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
