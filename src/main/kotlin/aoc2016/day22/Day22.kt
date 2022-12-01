@file:OptIn(ExperimentalStdlibApi::class)

package aoc2016.day22

import androidx.compose.desktop.ui.tooling.preview.Preview
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import java.awt.Dimension
import java.io.File
import java.util.PriorityQueue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import utils.Heading
import utils.InputFactory
import utils.NewGrid
import utils.Template
import utils.Vector

fun run() = runBlocking {
  val day = Day22(this)
  day.initialize()
  day.execute(withDelay = false)
  println(day.steps)
}

fun appMain() = application {
  Window(
    onCloseRequest = ::exitApplication
  ) {
    window.minimumSize = Dimension(1000, 1000)

    val scope = rememberCoroutineScope()
    val day = remember(scope) { Day22(scope) }

    DisposableEffect(Unit) {
      day.initialize()
      onDispose {  }
    }

    app22(day)
  }
}

private const val PATH_FADE = 20

class Day22(private val scope: CoroutineScope) {
  private var job: Job? = null
  private val realInput =
    File("/Users/dkhawk/Documents/advent-of-code/2021/src/main/resources/2016/22.txt").readLines()

  @Template("/dev/grid/node-x#0-y#1\\s*#2T\\s*#3T\\s*#4T\\s*#5%")
  data class Node(
    val x: Int,
    val y: Int,
    val size: Int,
    val used: Int,
    val avail: Int,
    val percent: Int,
  )

  private val inputFactory = InputFactory(Node::class)

  data class NamedNode(
    val name: String,
    val id: Int,
    val size: Int,
    val used: Int,
    val avail: Int,
    val percent: Int,
    val blocker: Boolean = false,
  ) {
    fun setUsed(newUsed: Int): NamedNode = copy(
      used = newUsed,
      avail = size - newUsed,
      percent = (newUsed * 100) / size,
    )
  }

  private val testInput = """
    Filesystem            Size  Used  Avail  Use%
    /dev/grid/node-x0-y0   10T    8T     2T   80%
    /dev/grid/node-x0-y1   11T    6T     5T   54%
    /dev/grid/node-x0-y2   32T   28T     4T   87%
    /dev/grid/node-x1-y0    9T    7T     2T   77%
    /dev/grid/node-x1-y1    8T    0T     8T    0%
    /dev/grid/node-x1-y2   11T    7T     4T   63%
    /dev/grid/node-x2-y0   10T    6T     4T   60%
    /dev/grid/node-x2-y1    9T    8T     1T   88%
    /dev/grid/node-x2-y2    9T    6T     3T   66%
    """.trimIndent().split("\n").filterNot { it.isEmpty() }.drop(1)

  private fun part1() {
    /*
root@ebhq-gridcenter# df -h
Filesystem              Size  Used  Avail  Use%
/dev/grid/node-x0-y0     85T   72T    13T   84%
     */

    val input = realInput.drop(2)//.take(5)

    val nodes = input
      .mapNotNull { inputFactory.lineToClass<Node>(it) }
      .mapIndexed {  index, node -> node.toNamedNode(index) }
    val viablePairs = viablePairs(nodes)

    println(viablePairs.size)
  }

  var width by mutableStateOf(-1)
  var height by mutableStateOf(-1)
  lateinit var nodes: List<Node>
  var maxSize by mutableStateOf(-1)
  var nodeData by mutableStateOf(emptyList<RenderNode>())
  var targetDataLocation by mutableStateOf(Vector(10, 10))
  var goalLocation by mutableStateOf(Vector(0, 0))
  var holeLocation by mutableStateOf(Vector(0, 0))
  var path = mutableStateListOf<Vector>()
  var paths = mutableStateListOf<List<Vector>>()

  var steps by mutableStateOf(0)

  var grid: NewGrid<NamedNode> = NewGrid(0, 0, emptyList())

  data class RenderNode(
    val location: Vector,
    val size: Int,
    val used: Int,
    val neighbors: List<Boolean>,
    val blocker: Boolean,
    val id: Int
  )

  fun initialize() {
    val input = realInput
    // val input = testInput

    nodes = input
      .mapNotNull { inputFactory.lineToClass<Node>(it) }
      .sortedWith(compareBy({ it.y }, { it.x }))

    width = (nodes.maxByOrNull { it.x }?.x?.plus(1) ?: throw Exception("WTF"))
    height = (nodes.maxByOrNull { it.y }?.y?.plus(1) ?: throw Exception("WTF"))
    targetDataLocation = Vector(width - 1, 0)
    maxSize = grid.data.maxByOrNull { it.size }?.size ?: -1

    initializeGrid()
  }

  private fun initializeGrid() {
    grid = NewGrid(width, height, nodes.mapIndexed { index, node -> node.toNamedNode(index) })
    holeLocation = grid.find { _, data -> data.used == 0 }!!.first

    gridToNodes()
  }

  private fun gridToNodes() {
    nodeData = grid.mapNotNull { location, node ->
      val neighbors = grid.getNeighbors(location).map { (_, nn) ->
        nn?.blocker == false
      }
      RenderNode(location, node.size, node.used, neighbors, node.blocker, node.id)
    }
  }

  suspend fun execute(withDelay: Boolean = false) {
    // While targetData location != goal location
    //   Move the "hole" to the "goal" location, while avoiding the "targetData" location
    //   Move the "hole" to the "targetData" location
    var goal = targetDataLocation.advance(Heading.WEST)

    while (targetDataLocation != goalLocation) {
      val holePath = findPath(holeLocation, goal, targetIsBlocker = true)
      holePath.forEach {
        path.add(it)
        moveHole(holeLocation, it)
        if (withDelay) delay(10)
      }
      path.clear()
      paths.add(0, holePath)

      if (withDelay) delay(20)

      val pathToTarget = findPath(holeLocation, targetDataLocation, targetIsBlocker = false)
      pathToTarget.forEach {
        path.add(it)
        moveHole(holeLocation, it)
        if (withDelay) delay(10)
      }
      path.clear()
      paths.add(0, pathToTarget)

      if (withDelay) delay(50)

      goal = targetDataLocation.advance(Heading.WEST)
    }

    repeat(PATH_FADE) {
      paths.add(0, emptyList())
      if (withDelay) delay(50)
    }
  }

  private fun moveHole(oldLocation: Vector, newLocation: Vector) {
    if (oldLocation == newLocation) return
    val holeNode = grid.getValue(oldLocation)
    val otherNode = grid.getValue(newLocation)

    grid[newLocation] = otherNode.setUsed(0)
    grid[oldLocation] = holeNode.setUsed(otherNode.used)

    holeLocation = newLocation
    if (newLocation == targetDataLocation) {
      targetDataLocation = oldLocation
    }

    steps += 1
  }

  private fun findPath(start: Vector, goal: Vector, targetIsBlocker: Boolean = false): List<Vector> {
    fun costHeuristic(v: Vector): Double = v.distance(goal)

    val nodeMap = nodeData.associateBy { it.location }

    val cameFrom = mutableMapOf<Vector, Vector>()

    val gScore = mutableMapOf<Vector, Double>().withDefault { Double.MAX_VALUE }
    gScore[start] = 0.0

    val fScoreMap = mutableMapOf<Vector, Double>().withDefault { Double.MAX_VALUE }
    fScoreMap[start] = costHeuristic(start)

    val openSet = PriorityQueue<Vector>(500, compareBy { fScoreMap[it] })
    openSet.add(start)

    var success = false

    while (openSet.isNotEmpty()) {
      val current = openSet.remove()
      if (current == goal) {
        success = true
        break
      }

      grid.getNeighbors(current).filter { (_, otherNode) ->
        if (otherNode != null) {
          otherNode.used < nodeMap[current]!!.size
        } else {
          false
        }
      }.filter { (otherLocation, _) ->
        if (targetIsBlocker) {
          otherLocation != targetDataLocation
        } else {
          true
        }
      }.forEach { (otherLocation, otherNode) ->
        val tentativeScore = gScore.getValue(current) + current.distance(otherLocation)
        if (tentativeScore < gScore.getValue(otherLocation)) {
          cameFrom[otherLocation] = current
          gScore[otherLocation] = tentativeScore
          val weight = costHeuristic(otherLocation)
          fScoreMap[otherLocation] = tentativeScore + weight

          if (otherLocation !in openSet) {
            openSet.add(otherLocation)
          }
        }
      }
    }

    if (success) {
      // reconstruct path here
      var current = goal
      val path = mutableListOf(current)
      while (current in cameFrom) {
        current = cameFrom[current]!!
        path.add(current)
      }
      return path.reversed()
    } else {
     throw Exception("No path found!")
    }
  }

  private fun printFormattedGrid(
    grid: NewGrid<NamedNode>,
    targetDataLocation: Vector,
    goal: Vector,
  ) {
    printGridSimple(grid) { location, node ->
      val c = when {
        location == targetDataLocation -> 'G'
        node.used == 0 -> '*'
        node.blocker -> '#'
        else -> '.'
      }

      when (location) {
        goal -> "($c)"
        else -> " $c "
      }
    }
  }

  private fun blockers(grid: NewGrid<NamedNode>): List<Vector> {
    val x = grid.map { location, node ->
      location to grid.getNeighbors(location).any { (nl, nn) ->
        (nn?.size ?: 0) >= node.used
      }
    }.filterNotNull()

    return x.filterNot { it.second }.map { it.first }
  }

  private fun printGrid(nodes: List<NamedNode>, width: Int) {
    val s = nodes.windowed(width, width) { row ->
      row.joinToString(" -- ") {
        String.format("%02d/%02d", it.used, it.size)
      }
    }.joinToString("\n")
    println(s)
  }

  private fun printGridSimple(grid: NewGrid<NamedNode>, formatter: (Vector, NamedNode) -> String) {
    val s = grid.map(formatter)

    val out = s.windowed(grid.width, grid.width) { row ->
      row.joinToString("")
    }.joinToString("\n")

    println(out)
  }

  private fun viablePairs(nodes: List<NamedNode>): MutableList<Pair<NamedNode, NamedNode>> {
    /*
      To do this, you'd like to count the number of viable pairs of nodes. A viable pair is any two
      nodes (A,B), regardless of whether they are directly connected, such that:

      Node A is not empty (its Used is not zero).
      Nodes A and B are not the same node.
      The data on node A (its Used) would fit on node B (its Avail).
     */
    // Can probably just brute force this!!

    val viablePairs = mutableListOf<Pair<NamedNode, NamedNode>>()
    nodes.forEach { aNode ->
      if (aNode.used != 0) {
        nodes.forEach { bNode ->
          if (aNode != bNode) {
            if (aNode.used <= bNode.avail) {
              viablePairs.add(aNode to bNode)
            }
          }
        }
      }
    }
    return viablePairs
  }

  fun start() {
    job?.cancel()
    job = scope.launch {
      execute()
    }
  }

  fun pause() {
    job?.cancel()
  }

  fun reset() {
    job?.cancel()
    path.clear()
    paths.clear()
    targetDataLocation = Vector(width - 1, 0)
    initializeGrid()
  }
}

private const val blockerThreshold = 100

private fun Day22.Node.toNamedNode(id: Int) =
  Day22.NamedNode(
    name = "node-x$x-y$y", id = id, size = size, used = used, avail = avail, percent = percent, blocker = used > blockerThreshold
  )

@Composable
@Preview
fun app22(day: Day22) {
  MaterialTheme {
    Surface(modifier = Modifier.background(Color.Black)) {

    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(16.dp)
    ) {
      Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Button(
          onClick = { day.start() },
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = "Start")
          Text("Start")
        }
        Button(onClick = { day.pause() }) {
          Icon(Icons.Default.Pause, contentDescription = "Pause")
          Text("Pause")
        }
        Button(onClick = { day.reset() }) {
          Icon(Icons.Default.RestartAlt, contentDescription = "Reset")
          Text("Reset")
        }
        Text(text = "Steps: ${ day.steps }")
      }

      val canvasSize = 2000.dp

      Box(
        modifier = Modifier
          .size(canvasSize)
          .padding(16.dp)
      ) {
        val size = 48F
        val padding = 8F

        renderGrid(size, day.nodeData, day.height, day.width, padding)

        drawPath(size, day.path)

        drawPaths(size, day.paths)

        renderTargets(day.holeLocation, day.goalLocation, day.targetDataLocation, size, padding)
      }
    }
  }
  }
}

@Composable
fun drawPaths(size: Float, paths: List<List<Vector>>) {
  val fadeRate = 1F / PATH_FADE
  paths.take(PATH_FADE).forEachIndexed { index, path ->
    val alpha = 1F - (fadeRate * index) // (1f - (1f + 0.1 * index)
    drawPath(size, path, Color.Blue.copy(alpha = alpha))
  }
}

@Composable
fun renderTargets(
  holeLocation: Vector,
  goalLocation: Vector,
  targetDataLocation: Vector,
  size: Float,
  padding: Float,
) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    drawGrid(goalLocation, size, padding, Color.Green)
    drawGrid(targetDataLocation, size, padding, Color(red = 1f, green = 0.75f, blue = 0f))
    drawHole(holeLocation, size, padding)
  }
}

private fun DrawScope.drawHole(
  holeLocation: Vector,
  size: Float,
  padding: Float,
) {
  val x = holeLocation.x * size
  val y = holeLocation.y * size
  drawCircle(
    color = Color.Black,
    radius = (size / 2) - padding,
    center = Offset(x + size / 2, y + size / 2)
  )
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

@Composable
private fun drawPath(size: Float, path: List<Vector>, color: Color = Color.Blue) {
  if (path.size > 1) {
    Canvas(
      modifier = Modifier.fillMaxSize()
    ) {
      val offset = size / 2
      val pathPoly = Path().apply {
        val start = path.first()
        moveTo(start.x.toFloat() * size + offset, start.y.toFloat() * size + offset)
        path.drop(1).forEach {
          lineTo(it.x.toFloat() * size + offset, it.y.toFloat() * size + offset)
        }
      }
      drawPath(pathPoly, color, style = Stroke(width = 5f))
    }
  }
}

@Composable
private fun renderGrid(
  size: Float,
  nodeList: List<Day22.RenderNode>,
  height: Int,
  width: Int,
  padding: Float
) {
  Canvas(
    modifier = Modifier.fillMaxSize()
  ) {
    nodeList.forEach { node ->
      val x = node.location.x.toFloat() * size
      val y = node.location.y.toFloat() * size

      drawRect(
        size = Size(size - 2 * padding, size - 2 * padding),
        topLeft = Offset(x + padding, y + padding),
        color = if (node.blocker) Color.Red else Color.LightGray,
        style = Fill
      )

      // drawIntoCanvas
      //
      // drawContext.canvas.nativeCanvas.apply {
      //   this.text
      //   this.drawTextLine(
      //     node.id.toString(),
      //     size.width / 2,
      //     size.height / 2,
      //     Paint().apply {
      //       textSize = 100
      //       color = Color.BLUE
      //       textAlign = Paint.Align.CENTER
      //     }
      //   )
      // }
      // d
    }

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
}
