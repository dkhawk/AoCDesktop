package aoc2022.day09

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sign
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import utils.Heading
import utils.InputNew
import utils.Vector

const val day = 9
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    R 4
    U 4
    L 3
    D 1
    R 4
    D 1
    L 5
    R 2
  """.trimIndent().split("\n").filter { it.isNotBlank() }

  val sample2 = """
    R 5
    U 8
    L 8
    D 3
    R 17
    D 10
    L 25
    U 20
  """.trimIndent().split("\n").filter { it.isNotBlank() }

  init {
  }

  // val bottomLeft = Vector(-437, -136)
  // val topRight = Vector(45, 73)

  var bottomLeft by mutableStateOf( Vector(-11 - 1,-15 - 1 ))
  var topRight by mutableStateOf( Vector(14 + 1,5 + 1))

  var showGrid by mutableStateOf(true)

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sample2
    }

    if (useRealData) {
      bottomLeft = Vector(-437 - 1, -136 - 1)
      topRight = Vector(45 + 1, 73 + 1)
      showGrid = false
    } else {
      bottomLeft = Vector(-11 - 1,-15 - 1)
      topRight = Vector(14 + 1,5 + 1)
      showGrid = true
    }

    input = lines
  }

  val dirMap = mapOf<String, Heading>(
    "U" to Heading.NORTH,
    "D" to Heading.SOUTH,
    "L" to Heading.WEST,
    "R" to Heading.EAST,
  )

  val threshold = 1.5 // Just needs to be bigger than sqrt(2)
  val sameLine = 2.01

  fun part1() {
    var head = Vector(0, 0)
    var tail = Vector(0, 0)

    val tailSet = mutableSetOf<Vector>()
    tailSet.add(tail)

    input.forEach { move ->
      val (s, t) = move.split(" ")
      val direction = dirMap.getValue(s)
      val distance = t.toInt()
      repeat(distance) {
        head = head.advance(direction)
        tail = moveTail(head, tail)
        tailSet.add(tail)
      }
    }

    println(tailSet.size)
  }

  fun part2() {
    val rope = mutableListOf<Vector>()
    val size = 10
    repeat(size) {
      rope.add(Vector(0, 0))
    }

    val tailSet = mutableSetOf<Vector>()
    tailSet.add(rope.last())

    val headSet = mutableSetOf<Vector>()
    headSet.add(rope.first())

    input.forEach { move ->
      val (s, t) = move.split(" ")
      val direction = dirMap.getValue(s)
      val distance = t.toInt()
      repeat(distance) {
        rope[0] = rope[0].advance(direction)
        headSet.add(rope.first())
        var headIndex = 0
        var tailIndex = 1

        while (tailIndex <= rope.lastIndex) {
          val new = moveTail(rope[headIndex], rope[tailIndex])
          if (new == rope[tailIndex]) {
            break
          }
          rope[tailIndex] = new
          headIndex++
          tailIndex++
        }
        tailSet.add(rope.last())
      }
    }

    // val headCoords = headSet.toSet()
    // val minX = headCoords.minOfOrNull { it.x }
    // val minY = headCoords.minOfOrNull { it.y }
    // val maxX = headCoords.maxOfOrNull { it.x }
    // val maxY = headCoords.maxOfOrNull { it.y }
    // println("$minX,$minY  $maxX,$maxY")

    println(tailSet.size)
  }

  private fun moveTail(head: Vector, tail: Vector): Vector {
    val dist = head.distance(tail)
    return if (dist < threshold) {
      tail
    } else {
      tail + tail.directionTo(head).sign()
    }
  }

  val visibleRope = mutableStateListOf<Vector>()
  var visitedSquares by mutableStateOf(emptySet<Vector>())
  var nextGoal by mutableStateOf(Vector(0, 0))

  fun step() {
  }

  fun execute() {
    job?.cancel()
    job = scope.launch {
      running = true
      // val rope = mutableListOf<Vector>()
      val size = 10
      repeat(size) {
        visibleRope.add(Vector(0, 0))
      }

      var iteration = 0

      val tailSet = mutableSetOf<Vector>()
      tailSet.add(visibleRope.last())

      val headSet = mutableSetOf<Vector>()
      headSet.add(visibleRope.first())

      var nextPause = 0

      input.forEach { move ->
        val (s, t) = move.split(" ")
        val direction = dirMap.getValue(s)
        val distance = t.toInt()
        repeat(distance) {
          visibleRope[0] = visibleRope[0].advance(direction)
          headSet.add(visibleRope.first())
          var headIndex = 0
          var tailIndex = 1

          while (tailIndex <= visibleRope.lastIndex) {
            visibleRope[tailIndex] = moveTail(visibleRope[headIndex], visibleRope[tailIndex])
            headIndex++
            tailIndex++
            if (useRealData) {
            if (iteration < 2_000) {
              val d = (iteration / 2_000f) * 5
              delay(d.toLong())
            }
            } else {
              delay(5)
            }
            iteration += 1
          }
          if (iteration < 5_000) {
            delay(1)
          } else if (iteration > nextPause) {
            delay(1)
            nextPause = iteration + 50
          }
          tailSet.add(visibleRope.last())
          visitedSquares = tailSet.toSet()
        }
      }
      running = false
    }
  }

  fun stop() {
    job?.cancel()
    running = false
  }

  fun reset() {
    stop()
    visibleRope.clear()
    visitedSquares = emptySet()
    nextGoal = Vector(0, 0)
  }

  fun updateDataSource(useRealData: Boolean) {
    this.useRealData = useRealData
    initialize()
    reset()
  }
}

private fun Vector.sign(): Vector = copy(
  x = this.x.sign,
  y = this.y.sign
)
