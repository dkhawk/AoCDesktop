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
  var delayTime by mutableStateOf( 500L)
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


  fun handToType(hand: String) : Type {
    return when {
      isFiveOfAKind(hand) -> Type.FIVE_OF_A_KIND
      isFourOfAKind(hand) -> Type.FOUR_OF_A_KIND
      isFullHouse(hand) -> Type.FULL_HOUSE
      isThreeOfAKind(hand) -> Type.THREE_OF_A_KIND
      isTwoPair(hand) -> Type.TWO_PAIR
      isOnePair(hand) -> Type.ONE_PAIR
      else -> Type.NONE
    }
  }

  private fun isFullHouse(hand: String): Boolean {
    if (useJokers) {
      val (jokers, rest) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        val groupCounts = rest.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
        return when (jokers.length) {
          1 -> groupCounts == listOf(2, 2)
          else -> false  // 4 is too many!  2 -> 4 of a kind
        }
      }
    }

    val groupCounts = hand.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
    return groupCounts == listOf(3, 2)
  }

  private fun isFourOfAKind(hand: String): Boolean {
    if (useJokers) {
      val (jokers, rest) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        val groupCounts = rest.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
        return when (jokers.length) {
          3 -> groupCounts.size == 2
          2 -> groupCounts == listOf(2, 1)
          1 -> groupCounts == listOf(3, 1)
          else -> false  // 4 is too many!
        }
      }
    }

    return hand.groupBy { it }.map { it.key to it.value.size }.any { it.second == 4 }
  }

  private fun isThreeOfAKind(hand: String): Boolean {
    if (useJokers) {
      val (jokers, rest) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        val groupCounts = rest.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
        return when (jokers.length) {
          2 -> groupCounts == listOf(1, 1, 1)
          1 -> groupCounts == listOf(2, 1, 1)
          else -> false
        }
      }
    }

    val groupCounts = hand.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
    return groupCounts == listOf(3, 1, 1)
  }

  private fun isOnePair(hand: String): Boolean {
    if (useJokers) {
      val (jokers, rest) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        val groupCounts = rest.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
        return when (jokers.length) {
          1 -> groupCounts == listOf(1, 1, 1, 1)
          else -> false
        }
      }
    }

    val groupCounts = hand.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
    return groupCounts == listOf(2, 1, 1, 1)
  }

  private fun isTwoPair(hand: String): Boolean {
    if (useJokers) {
      val (jokers, _) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        // Not possible to get two pair with jokers --> all possibilities can be better hands
        return false
      }
    }

    val groupCounts = hand.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
    return groupCounts == listOf(2, 2, 1)
  }

  private fun isFiveOfAKind(hand: String): Boolean {
    if (useJokers) {
      val (jokers, rest) = hand.partition { it == 'J' }
      if (jokers.isNotEmpty()) {
        // handle jokers
        if (jokers.length >= 4) {
          return true
        }
        val groupCounts = rest.groupBy { it }.map { it.key to it.value.size }.map { it.second }.sortedDescending()
        return groupCounts.size == 1
      }
    }

    val f = hand.first()
    return hand.all { it == f }
  }

  data class Hand(val cardString: String, val type: Type) : Comparable<Hand> {
    val comparableCardString = cardString.map {
      val cm = if (Day.useJokers) cardValuesWithJokers else cardValues
      when {
        it.isDigit() -> it
        else -> {
          '0' + cm.getValue(it)
        }
      }
    }.toString()

    override fun compareTo(other: Hand): Int {
      return compareBy(Hand::type, Hand::comparableCardString).compare(this, other)
    }
  }

  fun part1() {
    val bids = input.map { it.substring(0, 5) to it.substring(6).trim().toInt() }.toMap()
    val hands = bids.keys.map { Hand(it, handToType(it)) }.sorted()
    val winnings = hands.foldIndexed(0) { index, total, hand  ->
      (bids[hand.cardString]!! * (index + 1)) + total
    }
    println(winnings)
  }

  fun part2() {
    useJokers = true
    val bids = input.map { it.substring(0, 5) to it.substring(6).trim().toInt() }.toMap()
    val hands = bids.keys.map { Hand(it, handToType(it)) }.sorted()

    // println(hands.joinToString("\n"))
    // println(hands.map { it.comparableCardString }.joinToString("\n"))

    // println(handToType("JJQQA"))
    // printHand("JJQQQ")
    // printHand("JQQQQ")
    // printHand("JJQQA")
    // printHand("JJJQA")
    // printHand("JQAQA")
    // printHand("JJAQA")
    // printHand("JJJQK")
    // printHand("JJAKQ")
    // printHand("JAAKQ")

    val winnings = hands.foldIndexed(0) { index, total, hand  ->
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

    var useJokers = false

    val cardValues = mapOf(
      'A' to 14,
      'K' to 13,
      'Q' to 12,
      'J' to 11,
      'T' to 10,
    ).withDefault { it.digitToInt() }

    val cardValuesWithJokers = mapOf(
      'A' to 14,
      'K' to 13,
      'Q' to 12,
      'J' to 0,
      'T' to 10,
    ).withDefault { it.digitToInt() }
  }
}
