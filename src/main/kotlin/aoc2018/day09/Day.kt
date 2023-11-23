package aoc2018.day09

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
  """.trimIndent().split("\n")

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    // inputElves = input.mapIndexed { index, snacks -> toElf(index, snacks) }
  }

  class Node(val value: Int) {
    lateinit var prev: Node
    lateinit var next: Node
  }

  fun part1() {
    val lastMarble = 70904
    val numPlayers = 473

    playMarbleGame(numPlayers, lastMarble)
  }

  private fun playMarbleGame(numPlayers: Int, lastMarble: Int) {
    var currentPlayer = 0

    val scores = LongArray(numPlayers)

    var nextMarbleValue = 0
    var currentMable = Node(nextMarbleValue++)
    currentMable.next = currentMable
    currentMable.prev = currentMable

    // Remember the zeroMarble for later
    val zeroMarble = currentMable
    // printRound(currentPlayer, currentMable, zeroMarble)

    while (nextMarbleValue <= lastMarble) {
      currentPlayer = (currentPlayer + 1) % numPlayers
      val newNode = Node(nextMarbleValue++)
      if (newNode.value % 23 != 0) {
        val insertionSpot = currentMable.next.next

        newNode.next = insertionSpot
        newNode.prev = insertionSpot.prev

        newNode.prev.next = newNode
        newNode.next.prev = newNode

        currentMable = newNode
      } else {
        scores[currentPlayer] += newNode.value.toLong()
        var iter = currentMable
        repeat(7) {
          iter = iter.prev
        }

        iter.prev.next = iter.next
        iter.next.prev = iter.prev

        scores[currentPlayer] += iter.value.toLong()
        currentMable = iter.next
      }
      // printRound(currentPlayer, currentMable, zeroMarble)
    }

    println(scores.maxOf { it })
  }

  private fun printRound(currentPlayer: Int, currentMable: Node, zeroMarble: Node) {
    print("[$currentPlayer] ")

    var iter = zeroMarble

    do {
      val v = if (currentMable.value == iter.value) {
        "(${iter.value})"
      } else {
        " ${iter.value} "
      }
      print(v.padStart(4, ' '))

      iter = iter.next
    } while(iter.value != 0)
    println()

  }

  fun part2() {
    val lastMarble = 7090400
    val numPlayers = 473

    playMarbleGame(numPlayers, lastMarble)
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
