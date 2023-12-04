package aoc2023.day04

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.absoluteValue
import kotlin.math.pow
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
    Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
    Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
    Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
    Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
    Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
    Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11
  """.trimIndent().split("\n")

  var sampleInput2 : List<String>? = null

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else (
      if (sampleInput2 != null && part == 2)
        sampleInput2!!
      else
        sampleInput
    )
  }

  data class Card(
    val id: Int,
    val winningNumbers: List<Int>,
    val numbers: List<Int>
  ) {

    val matches: Int
      get() {
        val w = winningNumbers.toSet()
        val n = numbers.toSet()
        val matches = n.intersect(w)
        return matches.size
      }
  }

  fun Card(line: String) : Card {
    val parts = line.split(Regex("[:|]")).map { it.trim() }
    val id = parts[0].drop(5).trim().toInt()
    val winners = parts[1].split(Regex(" +")).map { it.trim().toInt() }
    val numbers = parts[2].split(Regex(" +")).map { it.trim().toInt() }

    return Card(id, winners, numbers)
  }

  fun part1() {
    val cards = input.map { Card(it) }

    val numMatches = cards.map { card -> card.matches }

    val score = numMatches.sumOf { if (it > 0) 2.0.pow(it.toDouble() - 1) else 0.0 }
    println(score)
  }

  data class CardStack(val id: Int, val count: Int)

  fun part2() {
    val cards = input.map { Card(it) }.map { it.copy(id = it.id - 1) }

    val cardQueue = cards.map { CardStack(it.id, 1) }.toMutableList()

    for (index in cardQueue.indices) {
      val cardStack = cardQueue[index]
      val numberOfCardsToMake = cards[cardStack.id].matches
      ((index + 1)until (index + 1 + numberOfCardsToMake)).forEach { id ->
        cardQueue[id] = cardQueue[id].copy(count = cardQueue[id].count + cardStack.count)
      }
    }

    println(cardQueue.sumOf { it.count })
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
