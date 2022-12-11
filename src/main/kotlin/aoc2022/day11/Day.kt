package aoc2022.day11

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.math.BigInteger
import java.util.TreeSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 11
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: String

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L


  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString()
      realInput
    } else {
      sampleInput
    }

    // inputElves = input.mapIndexed { index, snacks -> toElf(index, snacks) }
  }

  fun part1() {
    val monkeys = parseInput(input)
    // println(monkeys)

    repeat(20) {
      monkeys.forEach { monkey ->
        val actions = monkey.go()
        actions.forEach { (item, targetMonkey) ->
          monkeys[targetMonkey].receive(item)
        }
      }
    }

    println(monkeys.map { it.items }.joinToString("\n"))
    println(monkeys.map { it.inspectionCount }.joinToString("\n"))
    println()
    val ics = monkeys.sortedByDescending { it.inspectionCount }.take(2).map { it.inspectionCount }
    println(ics[0] * ics[1])
  }

  fun part2() {
    val monkeys = parseInput(input)
    println(monkeys.map { it.items })
    println(monkeys.map { it.operation })
    println(monkeys.map { it.test })
    // println(monkeys)

    return

    repeat(20) {
      monkeys.forEach { monkey ->
        val actions = monkey.go()
        actions.forEach { (item, targetMonkey) ->
          monkeys[targetMonkey].receive(item)
        }
      }
    }

    println(monkeys.map { it.items })

    println(monkeys.map { it.inspectionCount })

    val ics = monkeys.sortedByDescending { it.inspectionCount }.take(2).map { it.inspectionCount }
    println(ics[0])
    println(ics[1])
  }

  private fun parseInput(input: String): List<Monkey> {
    return input.split("\n\n").map { monkeyString ->
      parseMonkey(monkeyString)
    }
  }

  private fun parseMonkey(monkeyString: String): Monkey {
    // Keep it stupid
    val linesIter = monkeyString.split("\n").iterator()
    val id = parseId(linesIter.next())
    val items = parseItems(linesIter.next())
    val operation = parseOperation(linesIter.next())
    val test = linesIter.next().intAtEnd()
    val trueAction = linesIter.next().intAtEnd()
    val falseAction = linesIter.next().intAtEnd()

    return Monkey(id, items.toMutableList(), operation, test.toLong(), trueAction, falseAction)
  }

  private fun parseOperation(next: String): Operation {
    val expression = next.split(" = old ")[1]
    return when {
      expression == "* old" -> Operation.Square
      expression.startsWith("+") -> {
        val v = expression.split(" ")[1].toLong()
        Operation.AddFixed(v)
      }
      expression.startsWith("*") -> {
        val v = expression.split(" ")[1].toLong()
        Operation.MultFixed(v)
      }
      else -> throw Exception("Don't know how to parse $next")
    }
  }

  private fun parseItems(next: String): List<Long> {
    return next.split(":")[1].split(",").map { it.strip().toLong() }
  }

  private fun parseId(next: String): Int {
    return next.split(" ")[1].dropLastWhile { !it.isDigit() }.toInt()
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

class BigMonkey(
  val id: Int,
  val items: MutableList<BigInteger>,
  val operation: BigOperation,
  val test: BigInteger,
  val trueAction: Int,
  val falseAction: Int,
  var inspectionCount: Int = 0
) {
  fun go(): List<Pair<BigInteger, Int>> {
    val currentItems = items.toList()
    items.clear()
    inspectionCount += currentItems.count()
    return currentItems.map { operation(it) }.map { item ->
      item to (if (item % test == BigInteger.ZERO) trueAction else falseAction)
    }
  }

  fun receive(item: BigInteger) {
    items.add(item)
  }

  companion object {
    fun fromMonkey(monkey: Monkey) : BigMonkey {
      return BigMonkey(
        monkey.id,
        monkey.items.map { it.toBigInteger() }.toMutableList(),
        BigOperation.from(monkey.operation),
        monkey.test.toBigInteger(),
        monkey.trueAction,
        monkey.falseAction,
        monkey.inspectionCount
      )
    }
  }
}

class Monkey(
  val id: Int,
  val items: MutableList<Long>,
  val operation: Operation,
  val test: Long,
  val trueAction: Int,
  val falseAction: Int,
  var inspectionCount: Int = 0
) {
  fun go(): List<Pair<Long, Int>> {
    val currentItems = items.toList()
    items.clear()
    inspectionCount += currentItems.count()
    return currentItems.map { operation(it) }.map { it / 3 }.map { item ->
      item to (if (item % test == 0L) trueAction else falseAction)
    }
  }

  fun receive(item: Long) {
    items.add(item)
  }
}

private fun String.intAtEnd() = this.reversed().takeWhile { it.isDigit() }.reversed().toInt()

sealed class Operation {
  abstract operator fun invoke(old: Long): Long

  data class AddFixed(val addend: Long): Operation() {
    override fun invoke(old: Long) = addend + old
  }

  data class MultFixed(val multiplier: Long): Operation() {
    override fun invoke(old: Long) = multiplier * old
  }

  object Square: Operation() {
    override fun invoke(old: Long) = old * old
  }
}

sealed class BigOperation {
  abstract operator fun invoke(old: BigInteger): BigInteger

  data class AddFixed(val addend: BigInteger): BigOperation() {
    override fun invoke(old: BigInteger) = addend + old
  }

  data class MultFixed(val multiplier: BigInteger): BigOperation() {
    override fun invoke(old: BigInteger) = multiplier * old
  }

  object Square: BigOperation() {
    override fun invoke(old: BigInteger) = old * old
  }

  companion object {
    fun from(operation: Operation): BigOperation {
      return when(operation) {
        is Operation.AddFixed -> AddFixed(operation.addend.toBigInteger())
        is Operation.MultFixed -> MultFixed(operation.multiplier.toBigInteger())
        Operation.Square -> Square
      }
    }
  }
}

val sampleInput = """Monkey 0:
  Starting items: 79, 98
  Operation: new = old * 19
  Test: divisible by 23
    If true: throw to monkey 2
    If false: throw to monkey 3

Monkey 1:
  Starting items: 54, 65, 75, 74
  Operation: new = old + 6
  Test: divisible by 19
    If true: throw to monkey 2
    If false: throw to monkey 0

Monkey 2:
  Starting items: 79, 60, 97
  Operation: new = old * old
  Test: divisible by 13
    If true: throw to monkey 1
    If false: throw to monkey 3

Monkey 3:
  Starting items: 74
  Operation: new = old + 3
  Test: divisible by 17
    If true: throw to monkey 0
    If false: throw to monkey 1
"""
