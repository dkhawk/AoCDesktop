package aoc2023.day25

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
    jqt: rhn xhk nvd
    rsh: frs pzl lsr
    xhk: hfx
    cmg: qnr nvd lhk bvb
    rhn: xhk bvb hfx
    bvb: xhk hfx
    pzl: lsr hfx nvd
    qnr: nvd
    ntq: jqt hfx bvb xhk
    nvd: lhk
    lsr: lhk
    rzs: qnr cmg lsr rsh
    frs: qnr lhk lsr
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

  // Should figure out how to do this w/o graphviz.  :-)
  val linksToRemove = listOf(
    "gzr" to "qnz",
    "pgz" to "hgk",
    "lmj" to "xgs"
  ).map { it.toList().sorted() }.map { it[0] to it[1] }

  val sampleLinksToRemove = listOf(
    "hfx" to "pzl",
      "bvb" to "cmg",
      "nvd" to "jqt"
  ).map { it.toList().sorted() }.map { it[0] to it[1] }

  fun part1() {
    val allConnections = input.flatMap { line ->
      val node = line.substringBefore(":")
      val nodes = line.substringAfter(":").trim().split(" ").map { it.trim() }
      nodes.map { node to it }.map { it.toList().sorted() }.map { it[0] to it[1] }
    }

    val lr = if (useRealData) linksToRemove else sampleLinksToRemove

    val leftConnections = allConnections.filter { it !in lr }

    val graph = leftConnections.flatMap { listOf(it, it.second to it.first) }.groupBy({ it.first }, { it.second } )

    val visited = mutableSetOf<String>()
    val queue = ArrayDeque<String>()

    // Gotta start somewhere...
    queue.add(graph.keys.first())

    while (queue.isNotEmpty()) {
      val node = queue.removeFirst()
      visited.add(node)

      queue.addAll(
        graph.getValue(node).filter { it !in visited }
      )
    }

    val b = graph.entries.size - visited.size
    val a = visited.size

    println((b * a))
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
