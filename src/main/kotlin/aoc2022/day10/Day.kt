package aoc2022.day10

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid

const val day = 10
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    noop
    addx 3
    addx -5
  """.trimIndent().split("\n")

  init {
  }

  val interestingCycles = listOf(
    20, 60, 100, 140, 180, 220
  )

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sample2
    }
  }

  fun part1() {
    val ss = mutableListOf<Int>()

    val program = parseProgram(input)
    val computer = Computer(program)
    val icIter = interestingCycles.iterator()
    var nextInterestingCycle = icIter.next()

    while (!computer.done) {
      val x = computer.tick()
      val clock = computer.clock

      if (clock == nextInterestingCycle) {
        val signalStrength = clock * x
        ss.add(signalStrength)
        println("$clock * $x == $signalStrength")
        if (!icIter.hasNext()) break
        nextInterestingCycle = icIter.next()
      }
    }

    println(ss.sum())
  }

  fun part2() {
    val fill = mutableListOf<Char>()
    repeat(40 * 6) {
      fill.add('.')
    }

    val display = NewGrid<Char>(40, 6, fill)

    val program = parseProgram(input)
    val computer = Computer(program)

    var sprite: IntRange

    val currentRow = mutableListOf<Char>()

    while (!computer.done) {
      val clock = computer.clock
      val spriteMiddle = computer.tick()
      sprite = moveSprite(spriteMiddle)

      val pixel = if ((clock % 40) in sprite) {
        '#'
      } else {
        '.'
      }

      currentRow.add(pixel)
      display[clock % 240] = pixel

      // println("Start of cycle: $clock")
      // val spriteString = printSprite(sprite)
      // println("Sprite: $spriteString")
      // println("   row: ${currentRow.joinToString("")}")
      // println("End of $clock (x is ${computer.regX})")
      // println()
      // println()
      if (currentRow.size % 40 == 0) {
        println(currentRow.joinToString(""))
        currentRow.clear()
      }
    }

    println()
    println(display)
  }

  private fun printSprite(sprite: IntRange): String {
    val out = CharArray(40)
    repeat(40) {
      out[it] = '.'
    }
    sprite.forEach {
      out[it] = '#'
    }

    return out.joinToString("")
  }

  private fun moveSprite(x: Int): IntRange {
    return (x - 1)..(x + 1)
  }

  fun parseProgram(program: List<String>): List<Instruction> {
    return program.mapNotNull {
      val tokens = it.split(" ")
      val instruction = tokens.first()
      val args = tokens.drop(1)
      when (instruction) {
          "noop" -> Instruction.Noop
          "addx" -> Instruction.Addx(args.first().toInt())
        else -> null
      }
    }
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

class Computer(val program: List<Instruction>) {
  var previousX: Int = 0
  var regX: Int = 1
  var pc: Int = 0
  var clock: Int = 0
  var done = false

  private val programIterator = program.iterator()
  var currentInstruction: Instruction? = null

  fun step() {
    if (done) return

    if (pc < program.size) {
      previousX = regX
      val instruction = program[pc]
      instruction(this)
    }

    if (pc >= program.size) {
      done = true
    }
  }

  fun tick(): Int {
    if (done) return -1

    val xDuring = regX

    // Are we already executing an instruction?
    if (currentInstruction != null) {
      // println("finishing $currentInstruction")
      // finish the instruction
      currentInstruction?.invoke(this)
      currentInstruction = null
    } else {
      if (programIterator.hasNext()) {
        currentInstruction = programIterator.next()
        // println("new instruction $currentInstruction")
      } else {
        currentInstruction = null
        done = true  // might drop the last instruction this way.  :-(
      }
      if (currentInstruction?.numCycles == 1) {
        // this instruction takes one cycle, so we will need a new one next tick
        currentInstruction = null
      }
    }

    clock += 1

    return xDuring
  }
}

sealed class Instruction() {
  abstract val numCycles: Int
  abstract operator fun invoke(computer: Computer)

  object Noop: Instruction() {
    override val numCycles: Int = 1
    override operator fun invoke(computer: Computer) {
    }

    override fun toString(): String {
      return "noop"
    }
  }

  data class Addx(val value: Int): Instruction() {
    override val numCycles: Int = 2
    override operator fun invoke(computer: Computer) {
      computer.regX += value
    }

    override fun toString(): String {
      return "addx $value"
    }
  }
}

val sample2 = """
  addx 15
  addx -11
  addx 6
  addx -3
  addx 5
  addx -1
  addx -8
  addx 13
  addx 4
  noop
  addx -1
  addx 5
  addx -1
  addx 5
  addx -1
  addx 5
  addx -1
  addx 5
  addx -1
  addx -35
  addx 1
  addx 24
  addx -19
  addx 1
  addx 16
  addx -11
  noop
  noop
  addx 21
  addx -15
  noop
  noop
  addx -3
  addx 9
  addx 1
  addx -3
  addx 8
  addx 1
  addx 5
  noop
  noop
  noop
  noop
  noop
  addx -36
  noop
  addx 1
  addx 7
  noop
  noop
  noop
  addx 2
  addx 6
  noop
  noop
  noop
  noop
  noop
  addx 1
  noop
  noop
  addx 7
  addx 1
  noop
  addx -13
  addx 13
  addx 7
  noop
  addx 1
  addx -33
  noop
  noop
  noop
  addx 2
  noop
  noop
  noop
  addx 8
  noop
  addx -1
  addx 2
  addx 1
  noop
  addx 17
  addx -9
  addx 1
  addx 1
  addx -3
  addx 11
  noop
  noop
  addx 1
  noop
  addx 1
  noop
  noop
  addx -13
  addx -19
  addx 1
  addx 3
  addx 26
  addx -30
  addx 12
  addx -1
  addx 3
  addx 1
  noop
  noop
  noop
  addx -9
  addx 18
  addx 1
  addx 2
  noop
  noop
  addx 9
  noop
  noop
  noop
  addx -1
  addx 2
  addx -37
  addx 1
  addx 3
  noop
  addx 15
  addx -21
  addx 22
  addx -6
  addx 1
  noop
  addx 2
  addx 1
  noop
  addx -10
  noop
  noop
  addx 20
  addx 1
  addx 2
  addx 2
  addx -6
  addx -11
  noop
  noop
  noop
""".trimIndent().split("\n")

