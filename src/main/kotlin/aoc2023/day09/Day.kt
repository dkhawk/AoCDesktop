package aoc2023.day09

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
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  val sampleInput = """
    0 3 6 9 12 15
    1 3 6 10 15 21
    10 13 16 21 30 45
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

  fun part1() {
    val sum = input.map { line -> line.split(' ').map { item -> item.trim().toInt() } }
      .sumOf { line ->
        val lines = mutableListOf(line.toMutableList())
        while (lines.last().any { it != 0 }) {
          lines.add(lines.last().windowed(2, 1).map { (a, b) -> b - a }.toMutableList())
        }
        lines.reversed().fold(0) { acc, line ->
          (line.last() + acc).also { line.add(it) }
        }
        lines.first().last()
      }
    println(sum)
  }

  fun part2() {
    val sum = input.map { line -> line.split(' ').map { item -> item.trim().toLong() } }
      .sumOf { line ->
        val lines = mutableListOf(line.toMutableList())
        while (lines.last().any { it != 0L }) {
          lines.add(lines.last().windowed(2, 1).map { (a, b) -> b - a }.toMutableList())
        }

        lines.reversed().fold(0L) { acc, line ->
          (line.first() - acc).also { line.add(0, it) }
        }

        lines.first().first()
      }
    println(sum)
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
