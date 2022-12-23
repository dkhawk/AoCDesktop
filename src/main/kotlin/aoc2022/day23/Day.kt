package aoc2022.day23

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.Heading8
import utils.InputNew
import utils.NewGrid
import utils.Vector

const val day = 23
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  val sampleInput = """
    ....#..
    ..###.#
    #...#.#
    .#...##
    #.###..
    ##.#.##
    .#..#..
  """.trimIndent().split("\n")

  val sample2 = """
    .....
    ..##.
    ..#..
    .....
    ..##.
    .....
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
      // sample2
    }.filter { it.isNotBlank() }
  }

  data class Rule(val heading: Heading8) {
    operator fun invoke(elfMap: Collection<Vector>, elf: Vector): Vector? {
      val headings = listOf(heading.turnLeft(), heading, heading.turnRight())
      return if (headings.none { elf.advance(it) in elfMap }) {
        elf.advance(heading)
      } else {
        null
      }
    }
  }

  private val rules = listOf(
    Rule(Heading8.NORTH),
    Rule(Heading8.SOUTH),
    Rule(Heading8.WEST),
    Rule(Heading8.EAST),
  )

  fun part1() {
    val grid = NewGrid(input.first().length, input.size, input.joinToString("").toList())
    val elfMap = grid.findAll { _, c -> c == '#' }.map { it.first }.toMutableSet()

    val currentRuleList = ArrayDeque(rules)

    // printMap(elfMap)

    repeat(10) { iteration ->
      val proposed = elfMap.associateBy { it }.toMutableMap()

      elfMap.forEach { elf ->
        if (Heading8.values().any { elf.advance(it) in elfMap }) {
          currentRuleList.firstNotNullOfOrNull { rule -> rule(elfMap, elf) }?.let { move ->
            proposed[elf] = move
          }
        }
      }

      val moves = mutableMapOf<Vector, MutableList<Vector>>()

      proposed.forEach { (elf, move) ->
        moves.getOrPut(move, { mutableListOf() }).add(elf)
      }

      moves.forEach { (move, elves) ->
        if (elves.size > 1) {
          elves.forEach { elf ->
            proposed[elf] = elf
          }
        }
      }

      proposed.forEach { (elf, move) ->
        if (elf != move) {
          elfMap.remove(elf)
          elfMap.add(move)
        }
      }

      // println("$iteration")
      // printMap(elfMap)

      currentRuleList.addLast(currentRuleList.removeFirst())
    }

    println(countEmpties(elfMap))
  }

  private fun countEmpties(elfMap: MutableSet<Vector>): Int {
    val x0 = elfMap.minOfOrNull { it.x }!!
    val y0 = elfMap.minOfOrNull { it.y }!!
    val x1 = elfMap.maxOfOrNull { it.x }!!
    val y1 = elfMap.maxOfOrNull { it.y }!!

    return (y0..y1).sumOf { y ->
      (x0..x1).count { x ->
        Vector(x, y) !in elfMap
      }
    }
  }

  private fun printMap(elfMap: Collection<Vector>) {
    val x0 = elfMap.minOfOrNull { it.x }!! - 1
    val y0 = elfMap.minOfOrNull { it.y }!! - 1
    val x1 = elfMap.maxOfOrNull { it.x }!! + 1
    val y1 = elfMap.maxOfOrNull { it.y }!! + 1

    val s = (y0..y1).joinToString("\n") { y ->
      (x0..x1).joinToString("") { x ->
        if (elfMap.contains(Vector(x, y))) "#" else "."
      }
    }

    println(s)
  }

  fun part2() {
    val grid = NewGrid(input.first().length, input.size, input.joinToString("").toList())
    val elfMap = grid.findAll { _, c -> c == '#' }.map { it.first }.toMutableSet()

    val currentRuleList = ArrayDeque(rules)

    // printMap(elfMap)

    var elfMoved = true
    var rounds = 1

    while(elfMoved) {
      elfMoved = false
      val proposed = elfMap.associateBy { it }.toMutableMap()

      elfMap.forEach { elf ->
        if (Heading8.values().any { elf.advance(it) in elfMap }) {
          currentRuleList.firstNotNullOfOrNull { rule -> rule(elfMap, elf) }?.let { move ->
            proposed[elf] = move
          }
        }
      }

      val moves = mutableMapOf<Vector, MutableList<Vector>>()

      proposed.forEach { (elf, move) ->
        moves.getOrPut(move, { mutableListOf() }).add(elf)
      }

      moves.forEach { (move, elves) ->
        if (elves.size > 1) {
          elves.forEach { elf ->
            proposed[elf] = elf
          }
        }
      }

      proposed.forEach { (elf, move) ->
        if (elf != move) {
          elfMap.remove(elf)
          elfMap.add(move)
          elfMoved = true
        }
      }

      // println("$iteration")
      // printMap(elfMap)

      currentRuleList.addLast(currentRuleList.removeFirst())
      if (elfMoved) {
        rounds += 1
      }
    }

    println("rounds: $rounds")
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

private fun Collection<Vector>.containsNoneOf(
  location: Vector,
  headings: List<Heading8>,
): Boolean {
  val blockage = headings.firstOrNull { heading ->
    this.contains(location.advance(heading))
  }
  return blockage == null
}
