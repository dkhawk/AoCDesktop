package aoc2016.day23

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


sealed class Instruction(val args: List<String>) {
  abstract operator fun invoke(computer: Computer)

  abstract fun numberOfArgs(): Int

  private var name: String = ""
    get() {
      if (field.isEmpty()) {
        val n = this.javaClass.canonicalName!!
        val i = n.lastIndexOf('.')
        field = n.substring(i + 1).lowercase()
      }
      return field
    }

  override fun toString(): String {
    val aString = args.joinToString(" ")
    return "$name $aString"
  }
}

class Cpy(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    val rn = regNum(args.last())
    computer.registers[rn] = computer.value(args.first())
    computer.pc += 1
  }

  override fun numberOfArgs() = 2
}

class Inc(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    val registerNumber = regNumFromArgs(args)
    computer.registers[registerNumber] += 1L
    computer.pc += 1
  }

  override fun numberOfArgs() = 1
}

class Dec(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    val registerNumber = regNumFromArgs(args)
    computer.registers[registerNumber] -= 1L
    computer.pc += 1
  }

  override fun numberOfArgs() = 1
}

class Sub(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    val registerNumber = regNumFromArgs(args)
    computer.registers[registerNumber] -= computer.value(args.last())
    computer.pc += 1
  }

  override fun numberOfArgs() = 2
}

class Mul(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    computer.registers[regNumFromArgs(args)] *= computer.value(args.last())
    computer.pc += 1
  }

  override fun numberOfArgs() = 2
}

class Set(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    computer.registers[regNumFromArgs(args)] = computer.value(args.last())
    computer.pc += 1
  }

  override fun numberOfArgs() = 2
}

class Jnz(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    if (computer.value(args.first()) != 0L) {
      computer.pc += computer.value(args.last()).toInt()
    } else {
      computer.pc += 1
    }
  }

  override fun numberOfArgs() = 2
}

class Tgl(args: List<String>) : Instruction(args) {
  override fun invoke(computer: Computer) {
    /*
    tgl x toggles the instruction x away
      (pointing at instructions like jnz does: positive means forward; negative means backward):

    For one-argument instructions, inc becomes dec, and all other one-argument instructions become inc.
    For two-argument instructions, jnz becomes cpy, and all other two-instructions become jnz.
    The arguments of a toggled instruction are not affected.
    If an attempt is made to toggle an instruction outside the program, nothing happens.
    If toggling produces an invalid instruction (like cpy 1 2) and an attempt is later made to execute that instruction, skip it instead.
    If tgl toggles itself (for example, if a is 0, tgl a would target itself and become inc a), the resulting instruction
      is not executed until the next time it is reached.
    */

    val offset = computer.value(args.last()).toInt()
    val oldInstruction = computer.getInstruction(computer.pc + offset)
    if (oldInstruction == null) {
      computer.pc += 1
      return
    }

    val newInstruction = if (oldInstruction.numberOfArgs() == 1) {
      if (oldInstruction is Inc) {
        Dec(oldInstruction.args)
      } else {
        Inc(oldInstruction.args)
      }
    } else {
      if (oldInstruction is Jnz) {
        Cpy(oldInstruction.args)
      } else {
        Jnz(oldInstruction.args)
      }
    }

    computer.program[computer.pc + offset] = newInstruction

    computer.pc += 1
  }

  override fun numberOfArgs() = 1
}

fun parseProgram(program: List<String>) = program.map { parseInstruction(it) }

fun parseInstruction(line: String): Instruction {
  val parts = line.split(" ")

  val instruction = parts.first()
  val args = parts.drop(1)

  return when (instruction) {
    "set" -> Set(args)
    "mul" -> Mul(args)
    "sub" -> Sub(args)
    "jnz" -> Jnz(args)
    "cpy" -> Cpy(args)
    "inc" -> Inc(args)
    "dec" -> Dec(args)
    "tgl" -> Tgl(args)
    else -> throw Exception("Unrecognized instruction: '$instruction'")
  }
}

private fun regNumFromArgs(args: List<String>): Int = regNum(args.first())
private fun regNum(reg: String): Int = reg.first() - 'a'

private val numRegex = Regex("""-?[0-9][0-9]*""")

class Computer(private val scope: CoroutineScope) {
  private val resetSteps = mutableListOf<() -> Unit>()
  private val originalProgram = mutableListOf<Instruction>()

  val program = mutableStateListOf<Instruction>()
  val breakPoints = mutableStateMapOf<Int, Boolean>()

  var running by mutableStateOf(false)
  var pc by mutableStateOf(0)
  var stepDelay by mutableStateOf(500L)
  val maxDelay = 1000L

  val registers = mutableStateListOf<Long>()

  private var job: Job? = null

  init {
    repeat(4) {
      registers.add(0)
    }
  }

  fun loadProgram(program: List<Instruction>) {
    originalProgram.clear()
    originalProgram.addAll(program)
    reset()
  }

  fun value(valueString: String): Long {
    return if (numRegex.matches(valueString)) {
      valueString.toLong()
    } else {
      registers[valueString.first() - 'a']
    }
  }

  fun reset() {
    job?.cancel()
    program.clear()
    program.addAll(originalProgram)
    registers.indices.forEach { registers[it] = 0 }
    pc = 0
    running = false
    resetSteps.forEach { it() }
  }

  fun execute() {
    job?.cancel()
    job = scope.launch {
      running = true
      if (pc <= program.lastIndex) {
        step()
        delay(stepDelay)
      }
      while (pc <= program.lastIndex) {
        if (breakPoints.containsKey(pc)) {
          job?.cancel()
          running = false
          break
        }
        step()
        delay(stepDelay)
      }
    }
  }

  fun getInstruction(address: Int): Instruction? {
    return if (address < program.size)
      program[address]
    else
      null
  }

  fun step() {
    if (pc > program.lastIndex) {
      running = false
      println(pc)
      println(registers.joinToString(", "))
      return
    }

    val instruction = program[pc]
    // println(instruction)
    instruction.invoke(this)
    // println(pc)
    // println(registers.joinToString(", "))
    // println("----")
  }

  fun setBreakPoint(index: Int, isChecked: Boolean) {
    if (isChecked) {
      breakPoints[index] = true
    } else {
      breakPoints.remove(index)
    }
  }

  fun stop() {
    job?.cancel()
    running = false
  }

  fun addResetStep(resetStep: () -> Unit) {
    resetSteps.add(resetStep)
  }
}
