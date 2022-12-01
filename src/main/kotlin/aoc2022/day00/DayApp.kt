package aoc2022.day00

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.Stop
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension

fun appMain() = application {
  val scope = rememberCoroutineScope()
  val day = remember(scope) {
    Day(scope)
  }

  LaunchedEffect(Unit) {
    day.initialize()
  }

  Window(
    onCloseRequest = ::exitApplication
  ) {
    window.minimumSize = Dimension(1000, 1000)
    DayApp(day)
  }
}

@Preview
@Composable
fun DayApp(day: Day) {
  MaterialTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (true /* day.running */) {
          Button(
            onClick = { /* day.stop() */ },
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop")
            Text("Stop")
          }
        } else {
          Button(
            onClick = { /* day.execute() */ },
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run")
            Text("Run")
          }
        }
        Button(
          onClick = { /* day.step() */ },
        ) {
          Icon(Icons.Default.SkipNext, contentDescription = "Step")
          Text("Step")
        }
        // Button(onClick = { day.pause() }) {
        //   Icon(Icons.Default.Pause, contentDescription = "Pause")
        //   Text("Pause")
        // }
        Button(onClick = { /* day.reset() */ }) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
          Text("Reset")
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
