package aoc2018.day01

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.TreeSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 1
const val year = 2018

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    +1
    -2
    +3
    +1
  """.trimIndent().split("\n")

  val sampleInput2 = "+7, +7, -2, -7, -4".split(",").map { it.trim() }

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput2
    }
  }

  fun part1() {
    println(input.sumOf { it.toInt() })
  }

  fun part2() {
    val intList = input.map { it.toInt() }
    val seen = HashSet<Int>()
    var sum = 0
    seen.add(sum)
    while (true) {
      for (v in intList) {
        sum += v
        if (!seen.add(sum)) {
          println(sum)
          return
        }
      }
    }
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
