package aoc2022.day06

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.LinkedList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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

  val sampleInput = """mjqjpqmgbljsphdztnvjfqwrcgsmlb"""
  // val sampleInput = """nznrnfrfntjfmvfwmzdfjlvtqnbhcprsg"""
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

  fun part1a() {
    val answer = input.windowed(4, 1).withIndex().first {
      it.value.toSet().size == 4
    }

    println(answer.index + 4)
  }

  fun part2a() {
    val answer = input.windowed(14, 1).withIndex().first {
      it.value.toSet().size == 14
    }

    println(answer.index + 14)
  }

  fun part1b() {
    val answer = findFirstUniquePacket(4)
    println(answer)
  }

  fun part1() {
    println(
      findFirstUniquePacketWindowed(4)
    )
  }

  fun part2b() {
    val answer = findFirstUniquePacket(14)
    println(answer)
  }

  fun part2() {
    println(
      findFirstUniquePacketWindowed(14)
    )
  }

  private fun findFirstUniquePacket(windowSize: Int): Int {
    val letters = mutableMapOf<Char, Int>()
    val queue = BoundedQueue<Char>(windowSize)

    input.forEachIndexed { index, c ->
      letters[c] = letters[c]?.plus(1) ?: 1
      val removed = queue.add(c)
      if (removed != null) {
        val count = letters.getValue(removed)
        if (count == 1) {
          letters.remove(removed)
        } else {
          letters[removed] = count - 1
        }
      }

      if (letters.size == windowSize) {
        return index + 1
      }
    }
    return -1
  }

  private fun findFirstUniquePacketWindowed(windowSize: Int): Int {
    val letters = input.take(windowSize).groupingBy { it }.eachCount().toMutableMap()

    return input.windowed(windowSize + 1, 1).indexOfFirst { queue ->
      val removed = queue.first()
      val added = queue.last()
      letters[added] = letters[added]?.plus(1) ?: 1

      val count = letters.getValue(removed)
      if (count == 1) {
        letters.remove(removed)
      } else {
        letters[removed] = count - 1
      }

      letters.size == windowSize
    } + windowSize + 1
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

class BoundedQueue<E>(private val limit: Int) {
  val list = LinkedList<E>()

  fun add(value: E): E? {
    list.addFirst(value)
    return if (list.size > limit) {
      list.removeLast()
    } else {
      null
    }
  }

  override fun toString(): String {
    val s = list.joinToString(", ")
    return "$s (${list.size})"
  }
}