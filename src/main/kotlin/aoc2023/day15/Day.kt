package aoc2023.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.Template
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    rn=1,cm-,qp=3,cm=2,qp-,pc=4,ot=9,ab=5,pc-,pc=6,ot=7
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

  fun part1() {
    println(input.joinToString(",").split(",").sumOf { aocHash(it.trim()) })
  }

  fun part2() {
    val instructions = input.joinToString(",").split(",").map { it.trim().toOperation() }
    // println(instructions.joinToString("\n"))

    val boxes = List<Box>(256) { Box(it) }

    instructions.forEach { instruction ->
      val box = boxes[instruction.boxNumber]
      when (instruction) {
        is Instruction.Add -> box.add(instruction)
        is Instruction.Remove -> box.remove(instruction)
      }
    }

    println(boxes.sumOf { box -> box.focusPowers().sum() })
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

data class Lens(val label: String, var focalLength: Int) {
  override fun toString(): String {
    return "[$label $focalLength]"
  }
}

class Box(val boxNumber: Int) {
  private val lenses = mutableListOf<Lens>()

  fun add(instruction: Instruction.Add) {
    val l = lenses.find { it.label == instruction.boxString }
    if (l != null) {
      l.focalLength = instruction.focalLength
    } else {
      lenses.add(Lens(instruction.boxString, instruction.focalLength))
    }
  }

  fun remove(instruction: Instruction.Remove) {
    lenses.removeIf { it.label == instruction.boxString }
  }

  override fun toString(): String {
    return "Box $boxNumber: ${lenses.joinToString(" ")}"
  }

  fun focusPowers(): List<Long> {
    return lenses.mapIndexed { index, lens -> (boxNumber + 1) * (index + 1) * lens.focalLength.toLong()  }
  }
}

sealed class Instruction() {
  abstract val boxString: String
  abstract val boxNumber: Int

  data class Add(override val boxString: String, override val boxNumber: Int, val focalLength: Int): Instruction()

  data class Remove(override val boxString: String, override val boxNumber: Int): Instruction()
}

fun aocHash(s: String): Int {
  return s.map { it.code }.fold(0) { acc, ele ->
    ((acc + ele) * 17) % 256
  }
}

private fun String.toOperation(): Instruction {
  return if (contains('=')) {
    val (boxString, focalLength) = split("=")
    Instruction.Add(boxString, aocHash(boxString), focalLength.toInt())
  } else {
    val boxString = substringBefore("-")
    Instruction.Remove(boxString, aocHash(boxString))
  }
}
