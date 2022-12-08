package aoc2022.day08

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

const val day = 8
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: NewGrid<Int>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    30373
    25512
    65332
    33549
    35390
  """.trimIndent().split("\n").filter { it.isNotBlank() }

  init {
  }

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    input = NewGrid(lines.first().length, lines.size, lines.joinToString("").toList().map { it - '0' })
  }

  fun part1() {
    // Get each row and column
    val visibleTrees = mutableSetOf<Vector>()

    input.forEachRowIndexed { rowIndex, row ->
      visibleTrees.addAll(
        getVisibleTrees(row).map {
          Vector(it, rowIndex)
        }
      )
      visibleTrees.addAll(
        getVisibleTrees(row.reversed()).map {
          Vector((input.width - 1) - it, rowIndex)
        }
      )
    }

    input.forEachColumnIndexed { colIndex, col ->
      visibleTrees.addAll(
        getVisibleTrees(col).map {
          Vector(colIndex, it)
        }
      )
      visibleTrees.addAll(
        getVisibleTrees(col.reversed()).map {
          Vector(colIndex, (input.height - 1) - it)
        }
      )
    }

    println(visibleTrees.size)

    // val s = input.mapRowIndexed { rowIndex, row ->
    //   row.withIndex().joinToString("") { (colIndex, value) ->
    //     val location = Vector(colIndex, rowIndex)
    //     if (visibleTrees.contains(location)) {
    //       "[$value]"
    //     } else {
    //       " $value "
    //     }
    //   }
    // }
    // println(s.joinToString("\n"))
  }

  private fun getVisibleTrees(trees: List<Int>): MutableList<Int> {
    // start from the beginning
    val visible = mutableListOf<Int>()

    val iterator = trees.withIndex().iterator()
    var max = -1

    while (iterator.hasNext()) {
      val (index, tree) = iterator.next()
      if (tree > max) {
        visible.add(index)
        max = tree
      }
    }

    return visible
  }

  fun part2() {
    val scores = PriorityQueue<Int>(compareByDescending { it })

    (0 until input.height).forEach { rowIndex ->
      (0 until input.width).forEach { colIndex ->
        scores.add(
          viewingScore(getViewingDistances(colIndex, rowIndex))
        )
      }
    }

    println(scores.first())
  }

  private fun viewingScore(viewingDistances: List<Int>): Int {
    return viewingDistances.fold(1) { a, b ->
      a * b
    }
  }

  private fun getViewingDistances(colIndex: Int, rowIndex: Int): List<Int> {
    val location = Vector(colIndex, rowIndex)
    return Heading.values().map { heading ->
      getViewingDistanceForDirection(location, heading)
    }
  }

  private fun getViewingDistanceForDirection(
    location: Vector,
    heading: Heading,
  ): Int {
    val targetTree = input.getValue(location)
    var next = location
    var result = 0
    while (true) {
      next = next.advance(heading)
      if (input.validLocation(next)) {
        result += 1
        if (input.getValue(next) >= targetTree) {
          break
        }
      } else {
        break
      }
    }
    return result
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
