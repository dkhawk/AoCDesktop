package aoc2022.day15

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.Button
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope

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
    window.minimumSize = Dimension(500, 500)
    DayApp(day, scope)
  }
}

@Preview
@Composable
fun DayApp(day: Day, scope: CoroutineScope) {

  MaterialTheme {
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        if (day.running) {
          Button(
            onClick = { day.stop() },
          ) {
            Icon(Icons.Default.Stop, contentDescription = "Stop")
            Text("Stop")
          }
        } else {
          Button(
            onClick = { day.execute() },
          ) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Run")
            Text("Run")
          }
        }
        Button(
          onClick = { day.step() },
        ) {
          Icon(Icons.Default.SkipNext, contentDescription = "Step")
          Text("Step")
        }
        Button(onClick = {  day.reset() }) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
          Text("Reset")
        }
        Row(modifier = Modifier.padding(8.dp),
            verticalAlignment = Alignment.CenterVertically) {
          Checkbox(
            checked = day.useRealData,
            onCheckedChange = { day.updateDataSource(it) },
            enabled = true,
          )
          Text(text = "Use real input")
        }
        Column {
          Text(text = "step delay: ${day.delayTime}ms")
          Slider(
            value = day.delayTime.toFloat() / day.maxDelay.toFloat(),
            onValueChange = {
              day.delayTime = (it * day.maxDelay).toLong()
            }
          )
        }
      }
    }
  }
}
