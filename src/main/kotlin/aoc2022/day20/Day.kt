package aoc2022.day20

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.abs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 20
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<Int>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    1
    2
    -3
    3
    -2
    0
    4
    """.trimIndent().split("\n")

  init {
  }

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }.filter { it.isNotBlank() }

    input = lines.map { it.toInt() }
  }

  class Node(
    val value: Int,
    var index: Int,
  ) {
    lateinit var next: Node
    lateinit var prev: Node

    override fun toString(): String {
      return "$value"
    }
  }

  lateinit var head: Node

  fun part1() {
    val nodes = input.mapIndexed { index, i ->  Node(i, index) }
    nodes.windowed(2, 1) { (a, b) ->
      a.next = b
      b.prev = a
    }

    nodes.first().prev = nodes.last()
    nodes.last().next = nodes.first()

    head = nodes.first()

    val size = nodes.size
    val half = size / 2

    nodes.forEach { node ->
      if (node.value != 0) {
        var distance = node.value % size

        if (abs(distance) > half) {
          distance = -((size - 1) - distance)
        }

        if (distance != 0)
          moveNode(node, distance)
      }
      // printNodes(head)
      // println(head.value)
      // println()
    }

    // printNodes(head)

    // println("===================")

    val zero = nodes.first { it.value == 0 }

    val l = listOf(1000, 2000, 3000).map {
      getByIndex(zero, it, size)
    }
    println(l)
    println(l.sum())
  }

  private fun getByIndex(head: Node, i: Int, size: Int): Int {
    var node = head
    val distance = i // % size

    repeat(distance) {
      node = node.next
    }
    return node.value
  }

  private fun moveNode(node: Node, distance: Int) {
    // println("Move $node by $distance")
    val b = node

    // slice out of the current location
    // println("Was between ${b.prev} and ${b.next}")
    b.prev.next = b.next
    b.next.prev = b.prev

    var loc = b

    var c = loc
    var d = loc

    if (distance > 0) {
      repeat(distance) {
        loc = loc.next
      }
      c = loc
      d = loc.next
    } else if (distance < 0) {
      repeat(-distance) {
        loc = loc.prev
      }
      c = loc.prev
      d = loc
    }

    // println("Inserting between $c and $d")

    c.next = b
    b.prev = c

    d.prev = b
    b.next = d

    if (c == head) {
      head = b
    }
  }

  private fun printNodes(nodes: List<Node>) {
    printNodes(nodes.first())
  }

  private fun printNodes(head: Node) {
    var node = head
    val out = mutableListOf<Int>()
    do {
      out.add(node.value)
      node = node.next
    } while (node != head)

    println(out.joinToString(", ") { "$it" })
  }

  fun part2() {
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
