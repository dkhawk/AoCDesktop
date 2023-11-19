package aoc2018.day02

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
    abcdef
    bababc
    abbcde
    abcccd
    aabcdd
    abcdee
    ababab 
  """.trimIndent().split("\n")

  val sampleInput2 = """
    abcde
    fghij
    klmno
    pqrst
    fguij
    axcye
    wvxyz
  """.trimIndent().split("\n")

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput2
    }
  }

  fun part1() {
    val x = input.map { line ->
      val counts = line.trim().groupBy { it }.values.map { it.size }
      counts.contains(2) to counts.contains(3)
    }.unzip().toList().map { it.count { it } }

    println(x.first() * x.last())
  }

  fun part2() {
    var minDist = Int.MAX_VALUE
    var best = "" to ""

    input.forEachIndexed { index, s ->
      if (index < input.lastIndex) {
        input.subList(index + 1, input.size).forEach { other ->
          val d = s.distance(other)
          if (d < minDist) {
            minDist = d
            best = s to other
          }
        }
      }
    }

    println(minDist)
    println(best)
    println(best.first.intersection(best.second))
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

private fun String.intersection(other: String): String {
  return zip(other).mapNotNull { (a, b) -> if (a == b) a else null }.joinToString("")
}

private fun String.distance(other: String): Int {
  return zip(other).map { (a, b) -> a != b }.count { it }
}
