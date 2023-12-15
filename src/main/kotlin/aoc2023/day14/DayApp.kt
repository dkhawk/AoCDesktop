package aoc2023.day14

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
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
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import aoc2016.day22.Day22
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import utils.Vector

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
        Button(onClick = { day.reset() }) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
          Text("Reset")
        }
        Row(
          modifier = Modifier.padding(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
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

      Spacer(modifier = Modifier.height(16.dp))

      drawGrid(day.data, day.width, day.height)
    }
  }
}

@Composable
fun drawGrid(data: MutableList<Char>, width: Int, height: Int) {
  val canvasSize = 500.dp
  val padding = 8F
  val size = with(LocalDensity.current) { canvasSize.toPx() / width }
  Canvas(
    modifier = Modifier.size(canvasSize).padding(16.dp)
  ) {
    renderGrid(size, height, width)
    data.forEachIndexed { index, c ->
      if (c == 'O') {
        drawRoundRock(index, width, size, padding)
      }
      if (c == '#') {
        drawSquareRock(index, width, size, padding)
      }
    }
  }
}

private fun DrawScope.drawRoundRock(
  index: Int,
  width: Int,
  size: Float,
  padding: Float,
) {
  val (x, y) = indexToCoordinates(index, width, size)
  drawCircle(
    color = Color.Red,
    radius = (size / 2) - padding,
    center = Offset(x + size / 2, y + size / 2)
  )
}

private fun DrawScope.drawSquareRock(
  index: Int,
  width: Int,
  size: Float,
  padding: Float,
) {
  val (x, y) = indexToCoordinates(index, width, size)
  drawRect(
    size = Size(size - 2 * padding, size - 2 * padding),
    topLeft = Offset(x + padding, y + padding),
    color = Color.Black,
    style = Fill
  )
}

private fun indexToCoordinates(
  index: Int,
  width: Int,
  size: Float,
): Pair<Float, Float> {
  val row = index / width
  val col = index % width
  val x = size * col
  val y = size * row
  return Pair(x, y)
}

private fun DrawScope.drawGrid(
  location: Vector,
  size: Float,
  padding: Float,
  color: Color,
) {
  val x = location.x * size
  val y = location.y * size
  drawRect(
    size = Size(size - 2 * padding, size - 2 * padding),
    topLeft = Offset(x + padding, y + padding),
    color = color,
    style = Fill
  )
}

private fun DrawScope.renderGrid(
  size: Float,
  height: Int,
  width: Int,
) {
    repeat(height) { row ->
      repeat(width) { col ->
        drawRect(
          size = Size(size, size),
          topLeft = Offset((size * col), (row * size)),
          color = Color.Blue,
          style = Stroke(1F)
        )
      }
    }
}
