package aoc2022.day06

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

const val day = 6
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: String

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  // val sampleInput = """mjqjpqmgbljsphdztnvjfqwrcgsmlb"""
  val sampleInput = """nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"""
  // val sampleInput = """mjqjpqmgbljsphdztnvjfqwrcgsmlb"""

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString()
      realInput
    } else {
      sampleInput
    }
  }

  fun part1() {
    val answer = input.windowed(4, 1).withIndex().first {
      it.value.toSet().size == 4
    }

    println(answer.index + 4)
  }

  fun part2() {
    val answer = input.windowed(14, 1).withIndex().first {
      it.value.toSet().size == 14
    }

    println(answer.index + 14)
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
