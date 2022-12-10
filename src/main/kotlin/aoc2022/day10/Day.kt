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

    // inputElves = input.mapIndexed { index, snacks -> toElf(index, snacks) }
  }

  fun part1() {
    val ss = mutableListOf<Int>()

    val program = parseProgram(input)
    val computer = Computer(program)
    val icIter = interestingCycles.iterator()
    var nextInterestingCycle = icIter.next()
    while (!computer.done) {
      computer.step()

      if (computer.clock >= nextInterestingCycle) {
        val x = when (computer.program[computer.pc - 1]) {
          is Instruction.Addx -> computer.previousX
          Instruction.Noop -> computer.regX
        }

        val signalStrength = nextInterestingCycle * x
        ss.add(signalStrength)
        if (!icIter.hasNext()) break
        nextInterestingCycle = icIter.next()
      }
    }

    println(ss)
    println(ss.sum())
  }

  fun part2() {
    val fill = mutableListOf<Char>()
    repeat(40 * 6) {
      fill.add('.')
    }

    val display = NewGrid<Char>(40, 6, fill)

    println(display)

    val program = parseProgram(input)
    val computer = Computer(program)

    var sprite = (computer.regX - 1)..(computer.regX + 1)

    while (!computer.done) {
      computer.tick()


      val x = when (computer.program[computer.pc - 1]) {
        is Instruction.Addx -> computer.previousX
        Instruction.Noop -> computer.regX
      }

      }
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

  // val nextInstruction = program.iterator()
  var currentInstruction: Instruction? = null
  // var currentInstructionFinishTime = 0

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

  fun tick() {
    if (done) return

    // Are we already executing an instruction?
    if (currentInstruction != null) {
      // finish the instruction
      currentInstruction?.invoke(this)
      currentInstruction = null
    } else {
      currentInstruction = program[pc]
    }
  }
}

sealed class Instruction(val numCycles: Int) {
  abstract operator fun invoke(computer: Computer)

  object Noop: Instruction(1) {
    override operator fun invoke(computer: Computer) {
      computer.clock += numCycles
      computer.pc += 1
    }
  }

  data class Addx(val value: Int): Instruction(2) {
    override operator fun invoke(computer: Computer) {
      computer.clock += numCycles
      computer.regX += value
      computer.pc += 1
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

