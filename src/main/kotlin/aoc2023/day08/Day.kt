package aoc2023.day08

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

  val sampleInput0 = """
    RL

    AAA = (BBB, CCC)
    BBB = (DDD, EEE)
    CCC = (ZZZ, GGG)
    DDD = (DDD, DDD)
    EEE = (EEE, EEE)
    GGG = (GGG, GGG)
    ZZZ = (ZZZ, ZZZ)
  """.trimIndent().split("\n")

  val sampleInput1 = """
    LLR
    
    AAA = (BBB, BBB)
    BBB = (AAA, ZZZ)
    ZZZ = (ZZZ, ZZZ)
  """.trimIndent().split("\n")

  val sampleInput = """
    LLR
    
    AAA = (BBB, BBB)
    BBB = (AAA, ZZZ)
    ZZZ = (ZZZ, ZZZ)
  """.trimIndent().split("\n")

  val sampleInputPart2 = """
    LR

    11A = (11B, XXX)
    11B = (XXX, 11Z)
    11Z = (11B, XXX)
    22A = (22B, XXX)
    22B = (22C, 22C)
    22C = (22Z, 22Z)
    22Z = (22B, 22B)
    XXX = (XXX, XXX)
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInputPart2
    }
  }

  fun part1() {
    val turns = readTurns()
    val nodes = readNodes()

    // println(nodes.entries.joinToString("\n") { (k, v) -> "$k -> $v" })

    var currentNode = "AAA"
    var nextTurnIndex = 0
    var steps = 0
    while (currentNode != "ZZZ") {
      val nextTurn = turns[nextTurnIndex]
      currentNode = nodes.getValue(currentNode).let {
        if (nextTurn == 'L') it.first else it.second
      }
      nextTurnIndex = (nextTurnIndex + 1) % turns.size
      steps += 1
    }

    println(steps)
  }

  private fun readNodes(): Map<String, Pair<String, String>> {
    return input.drop(1).filter { it.isNotBlank() }.associate { line ->
      val n = line.substring(0, 3)
      val l = line.substringAfter('(').substring(0, 3)
      val r = line.substringAfter(", ").substring(0, 3)
      n to (l to r)
    }
  }

  private fun readTurns(): List<Char> = input.first().toList()

  data class Point(val node: String, val turnIndex: Int)

  fun part2() {
    val turns = readTurns()
    val nodes = readNodes()

    // println(turns)
    // println(nodes)

    var currentNodes = nodes.keys.filter { it.last() == 'A' }

    val cycles = currentNodes.map {
      mutableMapOf<Point, MutableList<Long>>()
    }

    var nextTurnIndex = 0
    var steps = 0L

    val counts = currentNodes.map { node ->
      // Get to the 'Z' at the same step in the turns
      // Or get back to the start?
      var c = 0
      var n = node

      var repeats = 3

      println(turns.size)

      while (true) {
        val turn = turns[c % turns.size]
        n = nodes.getValue(n).let { if (turn == 'L') it.first else it.second }
        c++
        // if (n.endsWith('Z')) break
        if (n.endsWith('Z')) {
          println("$c: ${c % turns.size}")
          if (repeats-- < 0) break
        }
      }
      c
    }

    println(counts)
    // 15995167053923 -- need an LCM function!!!
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
