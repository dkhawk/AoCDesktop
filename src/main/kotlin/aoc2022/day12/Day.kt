package aoc2022.day12

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.PriorityQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid
import utils.Vector

const val day = 12
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: NewGrid<Int>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    Sabqponm
    abcryxxl
    accszExk
    acctuvwj
    abdefghi
   """.trimIndent().split("\n").filter { it.isNotBlank() }

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    input = NewGrid(lines.first().length, lines.size, lines.joinToString("").toList().map { it - 'a' })
  }

  fun part1() {
    val startValue = 'S' - 'a'
    val finishValue = 'E' - 'a'

    val start = input.find { vector, i -> i == startValue }!!.first
    val finish = input.find { vector, i -> i == finishValue }!!.first

    input[start] = 'a' - 'a'
    input[finish] = 'z' - 'a'

    val path = findPath(input, start, finish)
    // println(path)
    // The path includes the start!
    println(path.size - 1)
  }

  fun part2() {
    val startValue = 'S' - 'a'
    val finishValue = 'E' - 'a'

    val start = input.find { vector, i -> i == startValue }!!.first
    val finish = input.find { vector, i -> i == finishValue }!!.first

    input[start] = 'a' - 'a'
    input[finish] = 'z' - 'a'

    val starts = input.findAll { vector, i -> i == 0 }

    println("Checking ${starts.size} locations")

    val paths = starts.mapNotNull {
      try {
        val path = findPath(input, it.first, finish)
        // The path includes the start!
        val length = path.size - 1
        it to length
      } catch (e: Exception) {
        null
      }
    }

    println(paths.minByOrNull { it.second })
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

private fun findPath(grid: NewGrid<Int>, start: Vector, goal: Vector): List<Vector> {
  fun costHeuristic(v: Vector): Double = v.distance(goal) + (('z' - 'a') - grid.getValue(v))

  val cameFrom = mutableMapOf<Vector, Vector>()

  val gScore = mutableMapOf<Vector, Double>().withDefault { Double.MAX_VALUE }
  gScore[start] = 0.0

  val fScoreMap = mutableMapOf<Vector, Double>().withDefault { Double.MAX_VALUE }
  fScoreMap[start] = costHeuristic(start)

  val openSet = PriorityQueue<Vector>(500, compareBy { fScoreMap[it] })
  openSet.add(start)

  var success = false

  while (openSet.isNotEmpty()) {
    val current = openSet.remove()
    if (current == goal) {
      success = true
      break
    }

    val currentElevation = grid.getValue(current)

    grid.getNeighbors(current).filter { (_, otherNode) ->
      otherNode?.let { it <= (currentElevation + 1) } ?: false
    }.forEach { (otherLocation, otherNode) ->
      val tentativeScore = gScore.getValue(current) + current.distance(otherLocation)
      if (tentativeScore < gScore.getValue(otherLocation)) {
        cameFrom[otherLocation] = current
        gScore[otherLocation] = tentativeScore
        val weight = costHeuristic(otherLocation)
        fScoreMap[otherLocation] = tentativeScore + weight

        if (otherLocation !in openSet) {
          openSet.add(otherLocation)
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
