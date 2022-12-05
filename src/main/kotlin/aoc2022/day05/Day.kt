@file:OptIn(ExperimentalStdlibApi::class)

package aoc2022.day05

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputFactory
import utils.InputNew
import utils.Template

const val day = 5
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    [D]    
[N] [C]    
[Z] [M] [P]
 1   2   3 

move 1 from 2 to 1
move 3 from 1 to 3
move 2 from 2 to 1
move 1 from 1 to 2
"""

  private lateinit var moves: List<Move>
  private lateinit var stacks: MutableList<MutableList<Char>>

  init {
  }

  @Template("move #0 from #1 to #2")
  data class Move(val quantity: Int, val source: Int, val destination: Int)

  fun initialize() {
    val input = if (useRealData) InputNew(year, day).readAsString() else sampleInput

    val (stacksInput, movesInput) = input.split("\n\n")

    stacks = parseStacks(stacksInput)

    val inputFactory = InputFactory(Move::class)
    moves = movesInput.split("\n").mapNotNull { inputFactory.lineToClass<Move>(it) }.map {
      it.copy(source = it.source - 1, destination = it.destination - 1)
    }
  }

  private fun parseStacks(stacksInput: String): MutableList<MutableList<Char>> {
    val lines = stacksInput.split("\n").filter { it.isNotBlank() }
    val c = lines.last().last { it in '0'..'9' }
    val size = c.toString().toInt()
    println(size)

    val stacks = lines.dropLast(1).map { line ->
      line.windowed(4, 4, true).map { it[1] }
    }.transpose()

    stacks.forEach { stack ->
      stack.removeIf { !it.isLetter() }
    }

    return stacks
  }

  fun part1() {
    moves.forEach { move ->
      val source = stacks[move.source]
      val destination = stacks[move.destination]
      destination.addAll(source.subList(source.size - move.quantity, source.size).reversed())
      repeat(move.quantity) {
        source.removeAt(source.lastIndex)
      }
    }

    val answer = stacks.map { it.last() }.joinToString("")
    println(answer)
  }

  fun part2() {
    moves.forEach { move ->
      val source = stacks[move.source]
      val destination = stacks[move.destination]
      destination.addAll(source.subList(source.size - move.quantity, source.size))
      repeat(move.quantity) {
        source.removeAt(source.lastIndex)
      }
    }

    val answer = stacks.map { it.last() }.joinToString("")
    println(answer)
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

private fun List<List<Char>>.transpose(): MutableList<MutableList<Char>> {
  val stacks = mutableListOf<MutableList<Char>>()
  repeat(this.last().size) {
    stacks.add(mutableListOf())
  }

  this.reversed().forEach { line ->
    line.forEachIndexed { index, c -> stacks[index].add(c) }
  }

  return stacks
}
