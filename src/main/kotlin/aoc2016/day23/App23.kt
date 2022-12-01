package aoc2016.day23

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Preview
@Composable
fun App23(computer: Computer) {
  MaterialTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (computer.running) {
          Button(
            onClick = { computer.stop() },
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop")
            Text("Stop")
          }
        } else {
          Button(
            onClick = { computer.execute() },
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run")
            Text("Run")
          }
        }
        Button(
          onClick = { computer.step() },
        ) {
          Icon(Icons.Default.SkipNext, contentDescription = "Step")
          Text("Step")
        }
        // Button(onClick = { day.pause() }) {
        //   Icon(Icons.Default.Pause, contentDescription = "Pause")
        //   Text("Pause")
        // }
        Button(onClick = { computer.reset() }) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
          Text("Reset")
        }
        Slider(
          value = computer.stepDelay.toFloat() / computer.maxDelay,
          onValueChange = { it ->
            computer.stepDelay = (it * computer.maxDelay).toLong()
          }
        )
      }

      Spacer(modifier = Modifier.height(16.dp))
      drawComputer(computer)
    }
  }
}

@Preview
@Composable
fun drawComputer(
  computer: Computer
) {
  drawRegisters(computer.registers)
  Spacer(modifier = Modifier.height(8.dp))
  drawProgramCounter(computer.pc)
  Spacer(modifier = Modifier.height(8.dp))
  drawProgram(computer.program, computer.pc, computer.breakPoints) { index, isChecked ->
    computer.setBreakPoint(index, isChecked)
  }
}

@Composable
fun drawProgramCounter(pc: Int) {
  Card(modifier = Modifier.size(48.dp)) {
    Box(modifier = Modifier.fillMaxSize()) {
      Text(
        modifier = Modifier.align(Alignment.Center),
        text = pc.toString()
      )
    }
  }
}

@Preview
@Composable
fun drawProgram(
  program: List<Instruction>,
  pc: Int,
  breakPoints: Map<Int, Boolean>,
  setBreakPoint: (Int, Boolean) -> Unit
) {
  Column(
    modifier = Modifier.width(400.dp),
  ) {
    program.forEachIndexed { index, instruction ->
      Row(
        modifier = Modifier.background(
          if (index == pc) Color(red = 0f, green = 1f, blue = 1f) else MaterialTheme.colors.background
        ).padding(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          imageVector = Icons.Default.Stop,
          tint = if (breakPoints.containsKey(index)) Color.Black else Color.Transparent,
          contentDescription = null,
          modifier = Modifier.clickable {
             setBreakPoint(index, !breakPoints.containsKey(index))
          },
        )
        // Checkbox(
        //   checked = breakPoints.containsKey(index),
        //   onCheckedChange = {
        //     setBreakPoint(index, it)
        //   }
        // )
        Spacer(modifier = Modifier.width(8.dp))
        Text(modifier = Modifier.width(32.dp), text = "$index")
        Spacer(modifier = Modifier.width(8.dp))
        Text(modifier = Modifier.width(128.dp), text = "$instruction")
      }
    }
  }
}

@Preview
@Composable
private fun drawRegisters(registers: List<Long>) {
  Column {
    registers.forEachIndexed { index, reg ->
      Card(
        modifier = Modifier.padding(2.dp)
      ) {
        Row(
          modifier = Modifier.padding(8.dp).width(100.dp)
        ) {
          Text(text = ('a' + index).toString() + ": ")
          Text(text = reg.toString())
        }
      }
    }
  }
}
