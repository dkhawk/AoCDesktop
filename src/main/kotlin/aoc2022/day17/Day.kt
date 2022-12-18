package aoc2022.day17

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.max
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.Vector

const val day = 17
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: String

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  val sampleInput = """>>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>"""

  init {
  }

  var moveNumber = 0

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString().trim()
      realInput
    } else {
      sampleInput
    }
  }

  fun nextMove() = input[moveNumber++ % input.length]

  sealed class Rock {
    abstract val shape: List<Vector>

    object Plus : Rock() {
      override val shape = listOf(
        Vector(1, 0),
        Vector(0, 1),
        Vector(1, 1),
        Vector(2, 1),
        Vector(1, 2),
      )
    }

    object Ell : Rock() {
      override val shape = listOf(
        Vector(0, 0),
        Vector(1, 0),
        Vector(2, 0),
        Vector(2, 1),
        Vector(2, 2),
      )
    }

    object VLine : Rock() {
      override val shape = listOf(
        Vector(0, 0),
        Vector(0, 1),
        Vector(0, 2),
        Vector(0, 3),
      )
    }

    object HLine: Rock() {
      override val shape = listOf(
        Vector(0, 0),
        Vector(1, 0),
        Vector(2, 0),
        Vector(3, 0),
      )
    }

    object Square : Rock() {
      override val shape = listOf(
        Vector(0, 0),
        Vector(1, 0),
        Vector(0, 1),
        Vector(1, 1),
      )
    }
  }

  val rocks = listOf(
    Rock.HLine,
    Rock.Plus,
    Rock.Ell,
    Rock.VLine,
    Rock.Square
  )

  fun getRock(iteration: Int) = rocks[iteration % rocks.size]

  val movementVectorMap = mapOf(
    '>' to Vector(1, 0),
    '<' to Vector(-1, 0)
  )

  fun part1() {
    val field = mutableSetOf<Vector>()
    repeat(7) {
      field.add(Vector(it, 0))
    }
    var top = 0

    repeat(2022) { iteration ->
      val referenceRock = getRock(iteration)
      val y = top + 4
      val x = 2
      val start = Vector(x, y)

      var rock = referenceRock.shape.map { it + start }
      var rockStopped = false
      while (!rockStopped) {
        // printGame(rock, field)
        // println()
        val moveChar = nextMove()
        val movementVector = movementVectorMap.getValue(moveChar)
        // println(moveChar)
        // println(movementVector)
        // println()

        var movedRock = moveRock(rock, movementVector)
        if (validRockPosition(movedRock, field)) {
          rock = movedRock
        }

        // move down
        movedRock = moveRock(rock, Vector(0, -1))
        if (validRockPosition(movedRock, field)) {
          rock = movedRock
        } else {
          rockStopped = true
          field.addAll(rock)
          top = field.maxOf { it.y }
          // printGame(emptyList(), field)
          // println()
        }
      }
    }

    // printGame(emptyList(), field)
    // println()

    println(top)
  }

  private fun printGame(rock: List<Vector>, field: MutableSet<Vector>) {
    val rockMaxY = rock.maxOfOrNull { it.y } ?: 0
    val fieldMaxY = field.maxOf { it.y }
    val maxY = max(rockMaxY, fieldMaxY)
    val s = (0..maxY).reversed().map { y ->
      (0..6).joinToString("") { x ->
        val l = Vector(x, y)
        when {
          rock.contains(l) -> "@"
          field.contains(l) -> "#"
          else -> "."
        }
      }
    }.joinToString("\n")
    println(s)
  }

  private fun validRockPosition(movedRock: List<Vector>, field: MutableSet<Vector>): Boolean {
    return movedRock.all { it.x in 0..6 } && movedRock.none { it in field }
  }

  private fun moveRock(rock: List<Vector>, movementVector: Vector) = rock.map { it + movementVector }

  fun part2() {
    // 1000000000000
    // 1514285714288
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

/*
|..@@@@.|
|.......|
|.......|
|.......|
|....#..|
|....#..|
|....##.|
|##..##.|
|######.|
|.###...|
|..#....|
|.####..|
|....##.|
|....##.|
|....#..|
|..#.#..|
|..#.#..|
|#####..|
|..###..|
|...#...|
|..####.|
+-------+
 */