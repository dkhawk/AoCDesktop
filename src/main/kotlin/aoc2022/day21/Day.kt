@file:OptIn(ExperimentalStdlibApi::class, DelicateCoroutinesApi::class)

package aoc2022.day21

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.Custom
import utils.InputFactory
import utils.InputNew
import utils.Template

const val day = 21
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    root: pppw + sjmn
    dbpl: 5
    cczh: sllz + lgvd
    zczc: 2
    ptdq: humn - dvpt
    dvpt: 3
    lfqf: 4
    humn: 5
    ljgn: 2
    sjmn: drzm * dbpl
    sllz: 4
    pppw: cczh / lfqf
    lgvd: ljgn * ptdq
    drzm: hmdt - zczc
    hmdt: 32
  """.trimIndent().split("\n")

  abstract class Monkey {
    abstract val name: String

    // abstract fun flow(): Flow<Int>
    abstract suspend fun value(monkeys: Map<String, Monkey>): Long
  }

  @Template("#0: #1 #2 #3")
  class MathMonkey(override val name: String, val operand1: String, @Custom(".") val operation: Char, val operand2: String) : Monkey() {
    override suspend fun value(monkeys: Map<String, Monkey>): Long {
      val o1 = GlobalScope.async {
        monkeys.getValue(operand1).value(monkeys)
      }

      val o2 = GlobalScope.async {
        monkeys.getValue(operand2).value(monkeys)
      }

      return when (operation) {
        '+' -> o1.await() + o2.await()
        '-' -> o1.await() - o2.await()
        '*' -> o1.await() * o2.await()
        '/' -> o1.await() / o2.await()
        else -> throw Exception("Unknown operation $operation")
      }
    }

    override fun toString(): String {
      return "MathMonkey: $name: $operand1 $operation $operand2"
    }
  }

  @Template("#0: #1")
  class NumberMonkey(override val name: String, val value: Int) : Monkey() {
    override suspend fun value(monkeys: Map<String, Monkey>): Long {
      return value.toLong()
    }

    override fun toString(): String {
      return "NumberMonkey: $name $value"
    }
  }

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }

    input = lines.filter { it.isNotBlank() }
  }

  fun part1() = runBlocking {
    val mmFactory = InputFactory(MathMonkey::class)
    val mathMonkeys: List<MathMonkey> = input.mapNotNull { mmFactory.lineToClass(it) }

    val nmFactory = InputFactory(NumberMonkey::class)
    val numberMonkeys: List<NumberMonkey> = input.mapNotNull { nmFactory.lineToClass(it) }

    val monkeyMap = (mathMonkeys + numberMonkeys).associateBy { it.name }

    val rootValue = monkeyMap.getValue("root").value(monkeyMap)

    println(rootValue)
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
