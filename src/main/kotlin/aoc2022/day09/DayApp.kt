package aoc2022.day09

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension
import kotlin.math.min
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
        // Button(
        //   onClick = { day.step() },
        // ) {
        //   Icon(Icons.Default.SkipNext, contentDescription = "Step")
        //   Text("Step")
        // }
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
        // Column {
        //   Text(text = "step delay: ${day.delayTime}ms")
        //   Slider(
        //     value = day.delayTime.toFloat() / day.maxDelay.toFloat(),
        //     onValueChange = {
        //       day.delayTime = (it * day.maxDelay).toLong()
        //     }
        //   )
        // }
      }

      val range = day.topRight - day.bottomLeft
      val numColumns = range.x
      val numRows = range.y

      BoxWithConstraints(
        modifier = Modifier
          .fillMaxSize()
          .padding(16.dp)
      ) {

        println(maxWidth)
        println(maxHeight)

        val w = (maxWidth / numColumns).value
        val h = (maxHeight / numRows).value

        println(w)
        println(h)

        val size = min(w, h) * 2f  // Why???

        if (day.showGrid) {
          renderGrid(size, numColumns, numRows)
        } else {
          renderBoundary(size, numColumns, numRows)
        }
        renderTailVisited(size, day.visitedSquares, day.bottomLeft)
        renderRope(size, day.visibleRope, day.bottomLeft)
      }
    }
  }
}

@Composable
fun renderBoundary(size: Float, numColumns: Int, numRows: Int) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    val y = numRows * size
    val x = numColumns * size
    drawRect(
      size = Size(x, y),
      topLeft = Offset(0f, 0f),
      color = Color.Blue,
      style = Stroke(3F)
    )
  }
}

@Composable
fun renderRope(size: Float, rope: List<Vector>, bottomLeft: Vector) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    rope.forEach { visited ->
      val translated = (visited - bottomLeft)
      val y = translated.y * size
      val x = translated.x * size
      drawRect(
        size = Size(size, size),
        topLeft = Offset(x, y),
        color = Color.Red,
        style = Fill
      )
    }
  }
}

@Composable
fun renderTailVisited(size: Float, visitedSquares: Set<Vector>, bottomLeft: Vector) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    visitedSquares.forEach { visited ->
      val translated = (visited - bottomLeft)
      val y = translated.y * size
      val x = translated.x * size
      drawRect(
        size = Size(size, size),
        topLeft = Offset(x, y),
        color = Color.Green,
        style = Stroke(5F)
      )
      drawRect(
        size = Size(size, size),
        topLeft = Offset(x, y),
        color = Color.Green.copy(alpha = 0.65f),
        style = Fill
      )
    }
  }
}

@Composable
fun renderGrid(size: Float, numColumns: Int, numRows: Int) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    repeat(numRows) { row ->
      val y = row * size
      repeat(numColumns) { col ->
        val x = col * size
        drawRect(
          size = Size(size, size),
          topLeft = Offset(x, y),
          color = Color.Blue,
          style = Stroke(1F)
        )
      }
    }
  }
}
