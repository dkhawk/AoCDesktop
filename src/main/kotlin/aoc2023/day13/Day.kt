package aoc2023.day13

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid
import utils.Vector
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

  private fun verticalReflectionPoint(grid: NewGrid<Char>): Pair<Int, Int>? {
    var reflectionPoint: Pair<Int, Int>? = null
    for (topIndex in 0 until (grid.height - 1)) {
      val bottomIndex = topIndex + 1

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

  private fun verticalSmudgeCandidates(grid: NewGrid<Char>): List<Vector> {
    return buildList {
      for (leftIndex in 0 until (grid.width - 1)) {
        val rightIndex = leftIndex + 1

        var li = leftIndex
        var ri = rightIndex
        while (li >= 0 && ri < grid.width) {
          val leftColumn = grid.getColumn(li)
          val rightColumn = grid.getColumn(ri)

          val diffs = leftColumn.zip(rightColumn).mapIndexedNotNull { index, (left, right) ->
            if (left != right) {
              Vector(li, index) to Vector(ri, index)
            } else {
              null
            }
          }

          if (diffs.size == 1) {
            add(diffs.first().first)
            add(diffs.first().second)
          }

          li -= 1
          ri += 1
        }
      }
    }
  }

  private fun horizontalSmudgeCandidates(grid: NewGrid<Char>): List<Vector> {
    return buildList {
      for (topIndex in 0 until (grid.height - 1)) {
        val bottomIndex = topIndex + 1

        var ti = topIndex
        var bi = bottomIndex
        while (ti >= 0 && bi < grid.height) {
          val topRow = grid.getRow(ti)
          val bottomRow = grid.getRow(bi)

          val diffs = topRow.zip(bottomRow).mapIndexedNotNull { index, (left, right) ->
            if (left != right) {
              Vector(index, ti) to Vector(index, bi)
            } else {
              null
            }
          }

          if (diffs.size == 1) {
            add(diffs.first().first)
            add(diffs.first().second)
          }

          ti -= 1
          bi += 1
        }
      }
    }
  }

  fun part2() {
    loadData()

    val patternGrids = input.split("\n\n").filter { it.isNotEmpty() }.map {
      NewGrid.fromCollectionOfStrings(it.trim().split("\n"))
    }
    val smudgePoints = patternGrids.take(1).map { grid ->

      val hrp = horizontalReflectionPoint(grid)
      val vrp = verticalReflectionPoint(grid)

      println(hrp)
      println(vrp)

      val hc = horizontalSmudgeCandidates(grid).toSet()
      val vc = verticalSmudgeCandidates(grid).toSet()

      println("horizontal")
      println(hc.joinToString("\n"))
      println("vertical")
      println(vc.joinToString("\n"))

      println("intersection")
      val c = hc.intersect(vc)
      println(c.joinToString("\n"))

      println("=============")

      c
      // buildList {
      //   horizontalReflectionPoint(grid)?.let { hr ->
      //     add(Pair(ReflectionDirection.Horizontal, hr.first + 1))
      //   }
      //   verticalReflectionPoint(grid)?.let {vr ->
      //     add(Pair(ReflectionDirection.Vertical, vr.first + 1))
      //   }
      // }
    }

    println(smudgePoints.joinToString("\n"))

    // println(reflectionPoints)

    // val answer = reflectionPoints.flatten().sumOf { reflectionPoint ->
    //   when (reflectionPoint.first) {
    //     ReflectionDirection.Horizontal -> reflectionPoint.second
    //     ReflectionDirection.Vertical -> reflectionPoint.second * 100
    //   }
    // }
    //
    // println(answer)
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
