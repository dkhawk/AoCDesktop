package aoc2018.day11

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import utils.InputNew
import utils.NewGrid
import utils.Vector
import utils.packageToYearDay
import kotlinx.coroutines.*


class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    18
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

  class Cell(val location: Vector) {
    val rackId: Int = location.x + 10

    val power: Int
      get() {
        var powerLevel = rackId * location.y
        powerLevel += gridSerialNumber
        powerLevel *= rackId
        powerLevel /= 100
        powerLevel %= 10
        powerLevel -= 5
        return powerLevel
      }
  }

  fun part1() {
    gridSerialNumber = input.first().toInt()
    val power = (1 .. 300).flatMap { y ->
      (1 .. 300).map { x ->
        Cell(Vector(x, y)).power
      }
    }

    val grid = NewGrid(300, 300, power)

    val subCells = grid.windowedIndexed(3, 3).map { (topLeft, cell) ->
      topLeft to cell.data.sum()
    }

    val best = subCells.maxByOrNull { it.second }
    println(best?.first?.plus(Vector(1, 1)))
  }

  suspend fun part2() = coroutineScope {
    gridSerialNumber = input.first().toInt()
    val power = (1 .. 300).flatMap { y ->
      (1 .. 300).map { x ->
        Cell(Vector(x, y)).power
      }
    }

    val grid = NewGrid(300, 300, power)

    val jobs = (0 .. 300).map { windowSize ->
      async {
        maxSum(grid, windowSize)?.let {m ->
          val v = Triple(m.first + Vector(1,1), m.second, windowSize)
          println(v)
          v
        }
      }
    }

    val results = jobs.awaitAll()

    println("Done")
    println(results.maxByOrNull { it?.second ?: Int.MIN_VALUE })
  }

  private suspend fun maxSum(grid: NewGrid<Int>, windowSize: Int) = withContext(Dispatchers.Default) {
    grid.maxSum(windowSize)
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

  companion object {
    var gridSerialNumber = 8
  }
}

private fun NewGrid<Int>.maxSum(windowSize: Int): Pair<Vector, Int>? {
  return (0 until (this.height - windowSize)).flatMap { y0 ->
    (0 until (this.width - windowSize)).mapNotNull { x0 ->
      val location = Vector(x0, y0)
      cellValue(location, windowSize)
    }
  }.maxByOrNull { it.second }
}

private fun NewGrid<Int>.cellValue(
  location: Vector,
  windowSize: Int,
): Pair<Vector, Int> {
  val firstIndex = toIndex(location)
  return location to (0 until windowSize).sumOf { kernelRow ->
    val index = firstIndex + (kernelRow * this.width)
    val n = this.data.slice(index until (index + windowSize))
    n.sum()
  }
}

private fun NewGrid<Int>.windowedIndexed(windowWidth: Int, windowHeight: Int): List<Pair<Vector, NewGrid<Int>>> {
  return (0 until (this.height - windowHeight)).flatMap { y0 ->
    (0 until (this.width - windowWidth)).map { x0 ->
      val location = Vector(x0, y0)
      location to this.subGrid(location, windowWidth, windowHeight)!!
    }
  }
}
