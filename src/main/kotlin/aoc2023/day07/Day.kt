package aoc2023.day07

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
    32T3K 765
    T55J5 684
    KK677 28
    KTJJT 220
    QQQJA 483
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

  fun handToType(hand: String): Type {
    val (jokers, cards) = hand.partition { it == 'j' }

    if (jokers.length == 5) return Type.FIVE_OF_A_KIND

    val counts =
      cards.groupingBy { it }.eachCount().values.sortedDescending().mapIndexed { index, count ->
        if (index == 0) count + jokers.length else count
      }

    return when {
      counts.contains(5) -> Type.FIVE_OF_A_KIND
      counts.contains(4) -> Type.FOUR_OF_A_KIND
      counts.contains(3) && counts.contains(2) -> Type.FULL_HOUSE
      counts.contains(3) -> Type.THREE_OF_A_KIND
      counts.count { it == 2 } == 2 -> Type.TWO_PAIR
      counts.contains(2) -> Type.ONE_PAIR
      else -> Type.NONE
    }
  }

  data class Hand(val cardString: String, val type: Type) : Comparable<Hand> {
    val comparableCardString = cardString.map {
      '0' + cardValues.getValue(it)
    }.toString()

    override fun compareTo(other: Hand): Int {
      return compareBy(Hand::type, Hand::comparableCardString).compare(this, other)
    }
  }

  fun part1() {
    val bids = input.associate { it.substring(0, 5) to it.substring(6).trim().toInt() }
    solve(bids)
  }

  fun part2() {
    val bids =
      input.associate {
        it.substring(0, 5).replace('J', 'j') to it.substring(6).trim().toInt()
      }
    solve(bids)
  }

  private fun solve(bids: Map<String, Int>) {
    val hands = bids.keys.map { Hand(it, handToType(it)) }.sorted()
    val winnings = hands.foldIndexed(0) { index, total, hand ->
      (bids[hand.cardString]!! * (index + 1)) + total
    }
    println(winnings)
  }

  private fun printHand(s: String) {
    println("$s -> ${handToType(s)}")
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

  companion object {
    enum class Type {
      /*
      Five of a kind, where all five cards have the same label: AAAAA
  Four of a kind, where four cards have the same label and one card has a different label: AA8AA
  Full house, where three cards have the same label, and the remaining two cards share a different label: 23332
  Three of a kind, where three cards have the same label, and the remaining two cards are each different from any other card in the hand: TTT98
  Two pair, where two cards share one label, two other cards share a second label, and the remaining card has a third label: 23432
  One pair, where two cards share one label, and the other three cards have a different label from the pair and each other: A23A4
  High card, where all cards' labels are distinct: 23456
       */
      NONE,
      ONE_PAIR,
      TWO_PAIR,
      THREE_OF_A_KIND,
      FULL_HOUSE,
      FOUR_OF_A_KIND,
      FIVE_OF_A_KIND,
    }

    val cardValues = mapOf(
      'A' to 14,
      'K' to 13,
      'Q' to 12,
      'J' to 11,
      'T' to 10,
      'j' to 0
    ).withDefault { it.digitToInt() }
  }
}
