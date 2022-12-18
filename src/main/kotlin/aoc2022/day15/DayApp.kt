package aoc2022.day15

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension
import kotlinx.coroutines.CoroutineScope
import utils.Vector

fun appMain() = application {
  val scope = rememberCoroutineScope()
  val day = remember(scope) {
    Day(scope)
  }

  LaunchedEffect(Unit) {
    day.useRealData = true
    day.initialize()
    day.part2Init()
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
      Header(day)
      SimArea(day)
    }
  }
}

@Composable
private fun Header(day: Day) {
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

@Composable
private fun SimArea(day: Day) {
  val size = 1500.dp
    val xScale = (size.value / day.xRange) * 1f
    val yScale = (size.value / day.yRange) * 1f

    val colors = listOf(
      Color.Blue,
      Color.Red,
      Color.Green,
      Color.Cyan,
      Color.LightGray,
      Color.Yellow,
    )

    //
  // xScale = 50f
    // yScale = 50f

  val bound = day.bound

  Canvas(
      modifier = Modifier.size(size).padding(16.dp)
    ) {
      // clipRect {

        translate(0f, 0f) {
          drawRect(topLeft = Offset(0f, 0f),
                   size = Size(day.bound * xScale, day.bound * yScale),
                   color = Color.Magenta.copy(alpha = 0.2f),
                   style = Fill)
          drawRect(topLeft = Offset(0f, 0f),
                   size = Size(day.bound * xScale, day.bound * yScale),
                   color = Color.Black,
                   style = Stroke(3f))

          drawCircle(color = Color.Blue,
                     center = Offset(day.bound * xScale, day.bound * yScale),
                     radius = 7f)

          day.diamonds.forEachIndexed { index, diamond ->
            val path = Path().apply {
              moveTo(diamond.bottom.x * xScale, diamond.bottom.y * yScale)
              lineTo(diamond.right.x * xScale, diamond.right.y * yScale)
              lineTo(diamond.top.x * xScale, diamond.top.y * yScale)
              lineTo(diamond.left.x * xScale, diamond.left.y * yScale)
              lineTo(diamond.bottom.x * xScale, diamond.bottom.y * yScale)
            }

            val color = colors[index % colors.size]
            drawPath(path, color.copy(alpha = 0.4f), style = Fill)
            drawPath(path, color, style = Stroke(width = 1f))
          }
          // drawRect(size = Size(day.bound * xScale, day.bound * abs(yScale)),
          //          color = Color.Black,
          //          style = Stroke(width = 3f))
          //

          day.sensors.forEach {
            drawCircle(center = it.scaled(xScale, yScale),
                       color = Color.Black,
                       radius = 5f,
                       style = Fill)
          }

          day.beacons.forEach {
            drawCircle(center = it.scaled(xScale, yScale),
                       color = Color.Green,
                       radius = 5f,
                       style = Fill)
          }

          day.forwards.forEach { fs ->
            drawForward(fs, xScale, yScale, Color.Red.copy(alpha = 0.3f), bound)
          }

          day.backwards.forEach { fs ->
            drawBackward(fs, xScale, yScale, Color.Blue.copy(alpha = 0.3f), bound)
          }

          day.forwardCandidates.forEach { fs ->
            drawForward(fs, xScale, yScale, Color.Magenta, bound)
          }

          day.backwardCandidates.forEach { fs ->
            drawBackward(fs, xScale, yScale, Color.Magenta, bound)
          }

          day.intersections.forEach { intersection ->
            drawCircle(center = intersection.scaled(xScale, yScale),
                       color = Color.Blue.copy(alpha = 0.5f),
                       radius = 9f,
                       style = Fill)
          }

          drawCircle(center = day.answer.scaled(xScale, yScale),
                     color = Color.Magenta,
                     radius = 7f,
                     style = Fill)
        }
      }
    }
  // }
// }

private fun DrawScope.drawForward(
  fs: Int,
  xScale: Float,
  yScale: Float,
  color: Color,
  bound: Int,
) {
  val end = Vector(fs + bound, bound).scaled(xScale, yScale)
  drawVector(fs, xScale, yScale, color, end)
}

private fun DrawScope.drawBackward(
  fs: Int,
  xScale: Float,
  yScale: Float,
  color: Color,
  bound: Int,
) {
  val end = Vector(fs - bound, bound).scaled(xScale, yScale)
  drawVector(fs, xScale, yScale, color, end)
}

private fun DrawScope.drawVector(
  fs: Int,
  xScale: Float,
  yScale: Float,
  color: Color,
  end: Offset,
) {
  val start = Vector(fs, 0).scaled(xScale, yScale)

  drawCircle(center = start,
             color = color,
             radius = 5f,
             style = Fill)

  drawCircle(center = end,
             color = color,
             radius = 5f,
             style = Fill)

  drawLine(start = start, end = end, color = color, strokeWidth = 3f)
}

private fun Vector.scaled(xScale: Float, yScale: Float) = Offset(x * xScale, y * yScale)
