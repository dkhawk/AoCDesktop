package aoc2023.day06

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigInteger
import kotlin.math.sqrt
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
    Time:      7  15   30
    Distance:  9  40  200
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

  fun quadratic(a: Int, b: Int, c: Int): Pair<Double, Double> {
    // -b±√(b²-4ac))/(2a)
    val s = ((b * b) - (4 * a * c))
    val d = 2 * a
    val x1 = (-b + sqrt(s.toDouble())) / d
    val x2 = (-b - sqrt(s.toDouble())) / d
    return x1 to x2
  }

  fun part1() {
    val inputs = input.map { line ->
      line.substringAfter(':').trim().split(Regex(" +")).map { it.toInt() }
    }

    val times = inputs[0]
    val distances = inputs[1]

    val races = times.zip(distances)
    println(races)

    val wins = races.map {(totalTime, distance) ->
      (0..totalTime).count { buttonTime ->
        val travelTime = totalTime - buttonTime
        val speed = buttonTime
        travelTime * buttonTime > distance
      }
    }
    val answer = wins.fold(1) { a, b -> a * b }

    println(answer)
  }

  fun part2() {
    val inputs = input.map { line ->
      line.substringAfter(':').replace(" ", "").toBigInteger()
    }

    val time = inputs[0]
    val distance = inputs[1]

    println(time)
    println(distance)

    var guess = time / BigInteger.TWO

    var min = BigInteger.ZERO
    var max = guess

    do {
      guess = (min + max) / BigInteger.TWO
      val raceResult = raceDistance(guess, time)
      if (raceResult > distance) {
        // win
        max = guess
      } else {
        // loss
        min = guess
      }
    } while (min + BigInteger.ONE < max)

    val first = if (raceDistance(min, time) > distance) min else max
    println(first)

    min = time / BigInteger.TWO
    max = time

    do {
      guess = (min + max) / BigInteger.TWO
      val raceResult = raceDistance(guess, time)
      if (raceResult > distance) {
        // win
        min = guess
      } else {
        // loss
        max = guess
      }
    } while (min + BigInteger.ONE < max)

    val last = if (raceDistance(min, time) > distance) min else max
    println(last)

    println((last - first) + BigInteger.ONE)
  }

  private fun raceDistance(guess: BigInteger, time: BigInteger) = (time - guess) * guess

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
