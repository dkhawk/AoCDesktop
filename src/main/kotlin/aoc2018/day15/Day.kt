package aoc2018.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.CharGrid
import utils.InputNew
import utils.Vector
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    #########
    #G..G..G#
    #.......#
    #.......#
    #G..E..G#
    #.......#
    #.......#
    #G..G..G#
    #########
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

  sealed class Player(var location: Vector) {
    var hitPoints: Int = 200

    fun isDead() = hitPoints <= 0

    class Elf(location: Vector) : Player(location)

    class Goblin(location: Vector) : Player(location)
  }

  fun part1() {
    val grid = CharGrid(input)
    val elves = grid.findAll { _, c -> c == 'E' }.map {
      Player.Elf(grid.indexToVector(it.first))
    }.toMutableList()
    val goblins = grid.findAll { _, c -> c == 'G' }.map {
      Player.Goblin(grid.indexToVector(it.first))
    }.toMutableList()
    println(elves)
    println(goblins)

    while (true) {
      // Check for a winner

      if (goblins.all { it.isDead() } || elves.all { it.isDead() }) {
        break
      }

      // units.forEach { unit ->
      //   // If near enemy attack
      //   val opponents = if (unit.second == 'G') elves else goblins
      //
      //
      //   // Else try to move
      // }

      break
    }
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
