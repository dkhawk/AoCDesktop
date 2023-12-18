package aoc2023.day17

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.PriorityQueue
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
    2413432311323
    3215453535623
    3255245654254
    3446585845452
    4546657867536
    1438598798454
    4457876987766
    3637877979653
    4654967986887
    4564679986453
    1224686865563
    2546548887735
    4322674655533
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

  data class State(val location: Vector, val cameFrom: Heading, val straightCount: Int, val cost: Int) {
    fun toVisited(): Visited {
      return Visited(location, cameFrom, straightCount)
    }

    val distanceToFinish: Double = location.distance(goal)

    companion object {
      lateinit var goal: Vector
    }
  }

  data class Visited(val location: Vector, val cameFrom: Heading, val straightCount: Int)

  fun part1() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    State.goal = Vector(grid.width - 1, grid.height - 1)

    val start = State(
      location = Vector(0, 0),
      cameFrom = Heading.EAST, // Doesn't matter for the start; could be EAST or NORTH
      straightCount = 0,
      cost = 0
    )

    val queue = PriorityQueue(compareBy(
      State::cost,
      State::distanceToFinish,
      State::straightCount,
      State::cameFrom,
    ))

    queue.add(start)

    val visited = mutableSetOf<Visited>(start.toVisited())

    var finishingState: State? = null

    val cameFrom = mutableMapOf<State, State>()

    while (queue.isNotEmpty()) {
      val current = queue.remove()
      visited.add(current.toVisited())

      if (current.location == State.goal) {
        finishingState = current
        break
      }

      // Where can we go from here?
      grid.getValidNeighbors(current.location).map {(location, costC) ->
        val cameFrom = current.location.headingTo(location)
        val straightCount = if (cameFrom == current.cameFrom) current.straightCount + 1 else 0
        val cost = current.cost + costC.digitToInt()
        State(location, current.location.headingTo(location), straightCount, cost)
      }.filter {state ->
        if (state.straightCount > 3) {
          false
        } else {
          state.toVisited() !in visited
        }
      }.forEach { state->
        cameFrom[state] = current
        queue.add(state)
      }
    }

    if (finishingState == null) {
      println("WTF")
      return
    }

    val path = buildList<State> {
      var next = finishingState
      while (next != null) {
        add(next)
        next = cameFrom[next]
      }
    }.reversed()

    println(finishingState)

    // println(path.joinToString("\n"))

    path.forEach { step ->
      grid[step.location] = step.cameFrom.cameFromSymbol()
    }

    println(grid)
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

private fun Heading.cameFromSymbol(): Char {
  return when (this) {
    Heading.NORTH -> 'v'
    Heading.WEST -> '>'
    Heading.EAST -> '<'
    Heading.SOUTH -> '^'
  }
}
