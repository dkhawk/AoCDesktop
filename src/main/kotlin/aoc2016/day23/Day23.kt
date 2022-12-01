package aoc2016.day23

import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension
import java.io.File

val sample = """
    cpy 2 a
    tgl a
    tgl a
    tgl a
    cpy 1 a
    dec a
    dec a""".trimIndent().split("\n").filter(String::isNotBlank)

const val inputFile = "/Users/dkhawk/Documents/advent-of-code/2021/src/main/resources/2016/23.txt"

fun appMain() = application {

  val scope = rememberCoroutineScope()
  val computer = remember(scope) {
    Computer(scope)
  }

  LaunchedEffect(Unit) {
    computer.addResetStep { computer.registers[0] = 7 }
    val realInput = File(inputFile).readLines()
    val program = parseProgram(realInput)
    computer.loadProgram(program)
  }

  Window(
    onCloseRequest = ::exitApplication
  ) {
    window.minimumSize = Dimension(1000, 1000)
    App23(computer)
  }
}
