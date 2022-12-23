package aoc2022.day22

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.Heading
import utils.InputNew
import utils.NewGrid
import utils.Vector

const val day = 22
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  fun initialize() {
    input = if (useRealData) {
      faceSize = 50
      maxSize = faceSize - 1
      InputNew(year, day).readAsLines(filterBlankLines = false)
    } else {
      faceSize = 4
      maxSize = faceSize - 1
      val dir = InputNew(year, day).baseDirectory
      val fileName = "$dir/22-sample.txt"
      File(fileName).readLines()
    }
  }

  fun part1() {
    val mapLines = input.takeWhile { it.isNotBlank() }
    val commandLine = input.last()

    val grid = createGrid(mapLines)
    val warpMap = createWarpMap(grid)

    val commands = parseCommands(commandLine)

    var location = findStart(grid)
    var heading = Heading.EAST

    commands.forEach { (distance, rotation) ->
      var remaining = distance
      while (remaining > 0) {
        var newLoc = location.advance(heading)
        var c = grid[newLoc]!!
        if (c == ' ') {
          newLoc = warpMap[newLoc to heading]!!
          c = grid[newLoc]!!
        }

        if (c == '#') {
          remaining = 0
        } else {
          location = newLoc
          remaining -= 1
        }
      }

      heading = when (rotation) {
        'R' -> heading.turnRight()
        'L' -> heading.turnLeft()
        else -> heading
      }
    }

    println(location)
    println(heading)
    // Facing is 0 for right (>), 1 for down (v), 2 for left (<), and 3 for up (^)
    val headingScore = when (heading) {
      Heading.EAST -> 0
      Heading.SOUTH -> 1
      Heading.WEST -> 2
      Heading.NORTH -> 3
    }

    val password = (location.y * 1000) + (location.x * 4) + headingScore
    println(password)
  }

  private fun findStart(grid: NewGrid<Char>): Vector {
    return grid.find { loc, c -> c == '.' }!!.first
  }

  data class Command(
    val distance: Int,
    val rotation: Char
  )

  private fun parseCommands(commandLine: String): MutableList<Command> {
    val iter = commandLine.iterator()
    val number = mutableListOf<Char>()
    val commands = mutableListOf<Command>()
    while (iter.hasNext()) {
      val c = iter.next()
      when {
        c == 'R' -> {
          commands.add(Command(number.joinToString("").toInt(), c))
          number.clear()
        }
        c == 'L' -> {
          commands.add(Command(number.joinToString("").toInt(), c))
          number.clear()
        }
        c.isDigit() -> number.add(c)
        else -> {}
      }
    }

    commands.add(Command(number.joinToString("").toInt(), '.'))

    return commands
  }

  private fun createWarpMap(grid: NewGrid<Char>): MutableMap<Pair<Vector, Heading>, Vector> {
    // Create warp map
    val warpMap = mutableMapOf<Pair<Vector, Heading>, Vector>()

    grid.forEachRowIndexed { row, chars ->
      // Skip blank rows
      if (chars.all { it.isWhitespace() }) {
        return@forEachRowIndexed
      }

      val first = chars.indexOfFirst { !it.isWhitespace() }
      val last = chars.indexOfLast { !it.isWhitespace() }

      warpMap[Vector(first - 1, row) to Heading.WEST] = Vector(last, row)
      warpMap[Vector(last + 1, row) to Heading.EAST] = Vector(first, row)
    }

    grid.forEachColumnIndexed { col, chars ->
      // Skip blank rows
      if (chars.all { it.isWhitespace() }) {
        return@forEachColumnIndexed
      }

      val first = chars.indexOfFirst { !it.isWhitespace() }
      val last = chars.indexOfLast { !it.isWhitespace() }

      warpMap[Vector(col, first - 1) to Heading.NORTH] = Vector(col, last)
      warpMap[Vector(col, last + 1) to Heading.SOUTH] = Vector(col, first)
    }
    return warpMap
  }

  private fun createGrid(mapLines: List<String>): NewGrid<Char> {
    val width = mapLines.maxByOrNull { it.length }!!.length

    val fullLines = mapLines.map {
      it.padEnd(width, ' ')
    }.toMutableList()

    // Add one character of extra padding around the entire grid
    fullLines.add(0, "".padStart(width))
    fullLines.add(fullLines.first())


    val paddedGrid = fullLines.map { line ->
      " $line "
    }

    return NewGrid(
      width = width + 2,
      height = paddedGrid.size,
      paddedGrid.joinToString("").toList()
    )
  }

  fun part2() {
    val mapLines = input.takeWhile { it.isNotBlank() }
    val commandLine = input.last()

    val grid = createGrid(mapLines)
    val faceList = createFaces()
    // labelFaces(faceList, grid)

    val edges = createEdges()

    val warpMap = createOrientationWarpMap(faceList, edges, grid, drawEdges = false)

    // verifyWarpMapVisually(grid, warpMap)

    val commands = parseCommands(commandLine)

    var orientation = Orientation(findStart(grid), Heading.EAST)

    commands.forEach { (distance, rotation) ->
      var remaining = distance
      while (remaining > 0) {
        val newOrientation =  warpMap[orientation] ?: orientation.advance()
        val c = grid.getValue(newOrientation.location)
        if (c == '#') {
          remaining = 0
        } else {
          orientation = newOrientation
          remaining -= 1
        }
      }

      val newHeading = when (rotation) {
        'R' -> orientation.heading.turnRight()
        'L' -> orientation.heading.turnLeft()
        else -> orientation.heading
      }

      orientation = orientation.copy(heading = newHeading)
    }

    println(orientation)
    // Facing is 0 for right (>), 1 for down (v), 2 for left (<), and 3 for up (^)
    val headingScore = when (orientation.heading) {
      Heading.EAST -> 0
      Heading.SOUTH -> 1
      Heading.WEST -> 2
      Heading.NORTH -> 3
    }

    val password = (orientation.location.y * 1000) + (orientation.location.x * 4) + headingScore
    println(password)
  }

  private fun createEdges(): List<Edge> {
    return if (useRealData) {
      createEdgesReal()
    } else {
      createEdgesSample()
    }
  }

  private fun createEdgesSample(): MutableList<Edge> {
    val edges = mutableListOf<Edge>()

    edges.addAll(createEdgePair(0, Heading.WEST, 2, Heading.NORTH).toList())
    edges.addAll(createEdgePair(0, Heading.NORTH, 1, Heading.NORTH).toList())
    edges.addAll(createEdgePair(0, Heading.EAST, 5, Heading.EAST).toList())

    edges.addAll(createEdgePair(1, Heading.WEST, 5, Heading.SOUTH).toList())
    edges.addAll(createEdgePair(1, Heading.SOUTH, 4, Heading.SOUTH).toList())

    edges.addAll(createEdgePair(2, Heading.SOUTH, 4, Heading.WEST).toList())

    edges.addAll(createEdgePair(3, Heading.EAST, 5, Heading.NORTH).toList())

    return edges
  }

  private fun createEdgesReal(): MutableList<Edge> {
    val edges = mutableListOf<Edge>()

    edges.addAll(createEdgePair(0, Heading.NORTH, 5, Heading.WEST).toList())
    edges.addAll(createEdgePair(0, Heading.WEST, 3, Heading.WEST).toList())

    edges.addAll(createEdgePair(1, Heading.NORTH, 5, Heading.SOUTH).toList())
    edges.addAll(createEdgePair(1, Heading.SOUTH, 2, Heading.EAST).toList())
    edges.addAll(createEdgePair(1, Heading.EAST, 4, Heading.EAST).toList())

    edges.addAll(createEdgePair(2, Heading.WEST, 3, Heading.NORTH).toList())

    edges.addAll(createEdgePair(4, Heading.SOUTH, 5, Heading.EAST).toList())

    return edges
  }

  private fun verifyWarpMapVisually(
    grid: NewGrid<Char>,
    warpMap: Map<Orientation, Orientation>,
  ) {
    val g = NewGrid(grid.width, grid.height, grid.data)
    warpMap.toList()
      .shuffled().take(5)
      // .drop(4)
      // .take(1)
      .forEachIndexed { index, it ->
        val (start, end) = it
        val c = 'a' + index
        g[start.location] = c
        g[start.location.advance(start.heading)] = c
        g[end.location] = c
        g[end.location.advance(end.heading.opposite())] = c
      }

    println(g)
  }

  private fun labelFaces(
    faceList: MutableList<Face>,
    grid: NewGrid<Char>,
  ) {
    faceList.forEachIndexed { index, face ->
      ((face.y0)..(face.y1)).forEach { y ->
        (face.x0..face.x1).forEach { x ->
          grid[Vector(x, y)] = '0' + index
        }
      }
    }
  }

  private fun createOrientationWarpMap(
    faceList: List<Face>,
    edges: List<Edge>,
    grid: NewGrid<Char>,
    drawEdges: Boolean = false,
  ): Map<Orientation, Orientation> {
    val warpMap = mutableMapOf<Orientation, Orientation>()

    faceList.forEachIndexed { faceIndex, face ->
      Heading.values().forEach { heading ->
        val edge = edges.firstOrNull { it.face == faceIndex && it.direction == heading }
        if (edge != null) {
          val squares = getEdgesForFace(heading, face)
          squares.forEach { location ->
            if (drawEdges) grid[location] = 'x'
            val o = Orientation(location, heading)
            val transformed = edge.transform(face.relativeLocation(location))
            val absLocation = transformed.second.location
            val targetFace = faceList[transformed.first]
            val targetLocation = targetFace.globalLocation(absLocation)
            warpMap[o] = Orientation(targetLocation, transformed.second.heading)
          }
        }
      }
    }
    return warpMap
  }

  data class Face(val x0: Int, val y0: Int, val x1: Int, val y1: Int) {
    private val minLocation = Vector(x0, y0)

    fun relativeLocation(location: Vector) = location - minLocation
    fun globalLocation(location: Vector) = location + minLocation
  }

  data class Orientation(val location: Vector, val heading: Heading) {
    fun advance() = Orientation(location = location.advance(heading), heading)
  }

  data class Edge(val face: Int, val direction: Heading, val transform: (Vector) -> Pair<Int, Orientation>)

  sealed class Axis {
    object Vertical : Axis()
    object Horizontal : Axis()
  }

  sealed class Position {
    object Start: Position()
    object End: Position()
  }

  private fun createEdgePair(face0: Int, heading0: Heading, face1: Int, heading1: Heading): Pair<Edge, Edge> {
    val axis0 = heading0.toAxis()
    val axis1 = heading1.toAxis()

    val position0: Position = heading0.toPosition()
    val position1: Position = heading1.toPosition()

    val transform0 = { faceLocation: Vector ->
      val newHeading = heading1.opposite()
      val newLocation = rotateLocation(axis0, position0, axis1, position1, faceLocation)
      face1 to Orientation(newLocation, newHeading)
    }

    val transform1 = { faceLocation: Vector ->
      val newHeading = heading0.opposite()
      val newLocation = rotateLocation(axis1, position1, axis0, position0, faceLocation)
      face0 to Orientation(newLocation, newHeading)
    }

    return Edge(face0, heading0, transform0) to Edge(face1, heading1, transform1)
  }

  private fun rotateLocation(
    axis0: Axis,
    position0: Position,
    axis1: Axis,
    position1: Position,
    faceLocation: Vector
  ): Vector {
    return if (axis0 == axis1) {
      if (position0 != position1) {
        if (axis0 == Axis.Vertical) {
          Vector(faceLocation.x, maxSize - faceLocation.y)
        } else {
          TODO("Foo")
        }
      } else {
        if (axis0 == Axis.Vertical) {
          Vector(maxSize - faceLocation.x, faceLocation.y)
        } else {
          Vector(faceLocation.x, maxSize - faceLocation.y)
        }
      }
    } else {
      // Different axis
      if (position0 != position1) {
        Vector(maxSize - faceLocation.y, maxSize - faceLocation.x)
      } else {
        // No transform necessary
        Vector(faceLocation.y, faceLocation.x)
      }
    }
  }

  private fun createFaces(): MutableList<Face> {
    return if (useRealData) {
      createFacesReal()
    } else {
      createFacesSample()
    }
  }

  private fun createFacesReal(): MutableList<Face> {
    val faceList = mutableListOf<Face>()

    faceList.add(makeFace(51, 1, maxSize))
    faceList.add(makeFace(101, 1, maxSize))
    faceList.add(makeFace(51, 51, maxSize))
    faceList.add(makeFace(1, 101, maxSize))
    faceList.add(makeFace(51, 101, maxSize))
    faceList.add(makeFace(1, 151, maxSize))
    return faceList
  }

  private fun createFacesSample(): MutableList<Face> {
    val faceList = mutableListOf<Face>()

    faceList.add(makeFace(9, 1, maxSize))
    faceList.add(makeFace(1, 5, maxSize))
    faceList.add(makeFace(5, 5, maxSize))
    faceList.add(makeFace(9, 5, maxSize))
    faceList.add(makeFace(9, 9, maxSize))
    faceList.add(makeFace(13, 9, maxSize))
    return faceList
  }

  private fun getEdgesForFace(
    heading: Heading,
    face: Face,
  ) = when (heading) {
    Heading.NORTH -> {
      val y = face.y0
      (face.x0..face.x1).map { x -> Vector(x, y) }
    }
    Heading.EAST -> {
      val x = face.x1
      (face.y0..face.y1).map { y -> Vector(x, y) }
    }
    Heading.SOUTH -> {
      val y = face.y1
      (face.x0..face.x1).map { x -> Vector(x, y) }
    }
    Heading.WEST -> {
      val x = face.x0
      (face.y0..face.y1).map { y -> Vector(x, y) }
    }
  }

  private fun makeFace(x: Int, y: Int, faceSize: Int) =
    Face(x, y, x + faceSize, y + faceSize)

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

  companion object {
    var faceSize = 4
    var maxSize = faceSize - 1
  }
}

private fun Heading.toPosition(): Day.Position {
  return when (this) {
    Heading.NORTH -> Day.Position.Start
    Heading.SOUTH -> Day.Position.End
    Heading.EAST -> Day.Position.End
    Heading.WEST -> Day.Position.Start
  }
}

private fun Heading.toAxis(): Day.Axis {
  return when (this) {
    Heading.NORTH -> Day.Axis.Vertical
    Heading.SOUTH -> Day.Axis.Vertical
    Heading.EAST -> Day.Axis.Horizontal
    Heading.WEST -> Day.Axis.Horizontal
  }
}
