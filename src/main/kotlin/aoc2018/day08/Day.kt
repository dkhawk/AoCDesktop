package aoc2018.day08

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    // inputElves = input.mapIndexed { index, snacks -> toElf(index, snacks) }
  }

  data class Node(val children: List<Node>, val metadata: List<Int>) {
    fun metadataSum(): Int {
      return metadata.sum() + children.sumOf { it.metadataSum() }
    }

    fun getValue(): Int {
      val childValues = mutableMapOf<Int, Int>()

      return if (children.isEmpty()) {
        metadata.sum()
      } else {
        metadata.map { it - 1 }.sumOf { index ->
          childValues.getOrPut(index) {
            children.getOrNull(index)?.getValue() ?: 0
          }
        }
      }
    }
  }

  fun part1() {
    val numbers = input.first().split(' ').map { it.toInt() }
    val iter = numbers.iterator()
    val node = readNode(iter)

    val sum = node.metadataSum()
    println(sum)
  }

  private fun readNode(iter: Iterator<Int>): Node {
    val numChildren = iter.next()
    val numMetadata = iter.next()

    val children = (0 until numChildren).map {
      readNode(iter)
    }

    val metadata = (0 until numMetadata).map { iter.next() }

    return Node(children, metadata)
  }

  fun part2() {
    val numbers = input.first().split(' ').map { it.toInt() }
    val iter = numbers.iterator()
    val node = readNode(iter)

    val value = node.getValue()
    println(value)
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
