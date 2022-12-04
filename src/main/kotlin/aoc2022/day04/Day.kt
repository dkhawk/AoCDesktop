package aoc2022.day04

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 4
const val year = 2022

class Day(private val scope: CoroutineScope) {
  private lateinit var inputRanges: List<List<IntRange>>
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>
  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    2-4,6-8
    2-3,4-5
    5-7,7-9
    2-8,3-7
    6-6,4-6
    2-6,4-8""".trimIndent().split("\n")

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    inputRanges = input.map { line ->
      line.split(",").map { range ->
        val ends = range.split("-").map(String::toInt)
        ends.first()..ends.last()
      }
    }
  }

  fun part1() {
    val contained = inputRanges.count { ranges ->
      val (r1, r2) = ranges
      r1.first in r2 && r1.last in r2 || r2.first in r1 && r2.last in r1
    }
    println(contained)
  }

  fun part2() {
    val overlaps = inputRanges.count { ranges ->
      val (r1, r2) = ranges
      r1.first in r2 || r1.last in r2 || r2.first in r1 || r2.last in r1
    }
    println(overlaps)
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
