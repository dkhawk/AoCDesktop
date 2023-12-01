package aoc2023.day01

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
  var part: Int = 0
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    1abc2
    pqr3stu8vwx
    a1b2c3d4e5f
    treb7uchet
  """.trimIndent().split("\n")

  val sampleInput2 = """
    two1nine
    eightwothree
    abcone2threexyz
    xtwone3four
    4nineeightseven2
    zoneight234
    7pqrstsixteen
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      if (sampleInput2.isNotEmpty() && part == 2) sampleInput2 else sampleInput
    }
  }

  fun part1() {
    val answer = input.map { it.filter { it in '0'..'9' } }
      .map { "${it.first()}${it.last()}" }
      .sumOf { it.toInt() }
    println(answer)
  }

  private val numbers = listOf(
    "zero",
    "one",
    "two",
    "three",
    "four",
    "five",
    "six",
    "seven",
    "eight",
    "nine",
  )

  fun part2() {
    val answer = input.map { line ->
      firstDigit(line) to lastDigit(line)
    }.sumOf { (a, b) ->
      (a * 10) + b
    }

    println(answer)
  }

  private fun firstDigit(input: String): Int {
    val words = numbers
    input.indices.forEach { index ->
      if (input[index] in '0'..'9') {
        return input[index] - '0'
      }
      words.withIndex().firstOrNull { (numberIndex, numberWord) ->
        if (input.substring(index).startsWith(numberWord)) {
          return numberIndex
        } else {
          false
        }
      }
    }
    throw Exception("No digit or number word found: $input")
  }

  private fun lastDigit(input: String): Int {
    val words = numbers
    input.indices.reversed().forEach { index ->
      if (input[index] in '0'..'9') {
        return input[index] - '0'
      }
      words.withIndex().firstOrNull { (numberIndex, numberWord) ->
        if (input.substring(index).startsWith(numberWord)) {
          return numberIndex
        } else {
          false
        }
      }
    }
    throw Exception("No digit or number word found: $input")
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
