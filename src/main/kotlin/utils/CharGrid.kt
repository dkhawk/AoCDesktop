package utils

import kotlin.math.abs
import kotlin.math.sign
import kotlin.math.sqrt

data class Vector(val x: Int = 0, val y: Int = 0) : Comparable<Vector> {
  val sign: Vector
    get() = Vector(x.sign, y.sign)

  operator fun times(scale: Int): Vector {
    return Vector(x * scale, y * scale)
  }

  operator fun minus(other: Vector): Vector = Vector(x - other.x, y - other.y)
  operator fun plus(other: Vector): Vector = Vector(x + other.x, y + other.y)

  fun advance(heading: Heading): Vector {
    return this + heading.vector
  }

  fun directionTo(end: Vector): Vector = (end - this)

  fun distance(goal: Vector): Double {
    val dx = goal.x - x
    val dy = goal.y - y
    return sqrt(((x*x) + (y*y)).toDouble())
  }

  fun abs(): Vector {
   return Vector(abs(x), abs(y))
  }

  override fun compareTo(other: Vector): Int = compareValuesBy(this, other, { it.x }, { it.y })

  fun neighborsConstrained(top: Int = 0, bottom: Int, left: Int = 0, right: Int): List<Vector> {
    return Heading.values().mapNotNull { heading ->
      val candidate = this.advance(heading)
      if (candidate.y in top..bottom && candidate.x in left..right) {
        candidate
      } else {
        null
      }
    }
  }

  fun neighbors() = Heading.values().map { heading -> this.advance(heading) }
}

enum class Direction {
  NORTH, EAST, SOUTH, WEST
}

enum class Heading(val vector: Vector) {
  NORTH(Vector(0, -1)),
  EAST(Vector(1, 0)),
  SOUTH(Vector(0, 1)),
  WEST(Vector(-1, 0));

  fun turnRight(): Heading {
    return values()[(this.ordinal + 1) % values().size]
  }

  fun turnLeft(): Heading {
    return values()[(this.ordinal + values().size - 1) % values().size]
  }

  fun opposite() : Heading {
    return values()[(this.ordinal + 2) % values().size]
  }

  fun turnTo(other: Heading): Int {
    return other.ordinal - ordinal
  }
}

data class Vector3dLong(val x: Long, val y: Long, val z: Long) {
  operator fun plus(other: Vector3dLong): Vector3dLong = Vector3dLong(x + other.x, y + other.y, z + other.z)

  fun distanceTo(other: Vector3dLong): Long = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

  override fun toString(): String = "<$x,$y,$z>"
}

data class Vector3d(val x: Int, val y: Int, val z: Int) {
  operator fun plus(other: Vector3d): Vector3d = Vector3d(x + other.x, y + other.y, z + other.z)
  operator fun minus(other: Vector3d): Vector3d = Vector3d(x - other.x, y - other.y, z - other.z)

  fun distanceTo(other: Vector3d): Int = abs(x - other.x) + abs(y - other.y) + abs(z - other.z)

  fun abs(): Vector3d {
    return Vector3d(abs(x), abs(y), abs(z))
  }

  override fun toString(): String = "<$x,$y,$z>"
}

enum class Heading8(val vector: Vector) {
  NORTH(Vector(0, -1)),
  NORTHEAST(Vector(1, -1)),
  EAST(Vector(1, 0)),
  SOUTHEAST(Vector(1, 1)),
  SOUTH(Vector(0, 1)),
  SOUTHWEST(Vector(-1, 1)),
  WEST(Vector(-1, 0)),
  NORTHWEST(Vector(-1, -1));

  fun turnRight(): Heading8 {
    return values()[(this.ordinal + 1) % values().size]
  }

  fun turnLeft(): Heading8 {
    return values()[(this.ordinal + values().size - 1) % values().size]
  }
}

class NewGrid<T>(var width: Int, var height: Int, data: Collection<T>) {
  var data : MutableList<T>

  init {
    this.data = data.toMutableList()
  }

  fun <R> map(function: (Vector, T) -> R?): List<R?> {
    return data.mapIndexed { index, t ->
      function(Vector(index % width, index / width), t)
    }
  }

  fun <R> mapNotNull(function: (Vector, T) -> R): List<R> {
    return data.mapIndexed { index, t ->
      function(toLocation(index), t)
    }
  }

  fun getNeighbors(location: Vector) = location.neighbors().map { it to this[it] }

  fun getNeighborsConstrained(location: Vector) =
    location.neighborsConstrained(bottom = height - 1, right = width - 1).map { it to this[it] }
  //
  // fun <R> mapNeighbors(function: (Pair<Vector, T>, Pair<Vector, T>) -> R?): List<Pair<Vector, List<Pair<Vector, R?>>>> {
  //   return data.mapIndexed { index, t ->
  //     val loc = toLocation(index)
  //     val current = loc to t
  //     loc to loc.neighborsConstrained(bottom = height - 1, right = width - 1).map {
  //       it to function(
  //         current,
  //         it to get(it)
  //       )
  //     }
  //   }
  // }

  operator fun get(location: Vector): T? {
    return if (location.x < 0 || location.x >= width || location.y < 0 || location.y >= height) {
      null
    } else {
      data[toIndex(location)]
    }
  }

  operator fun set(location: Vector, value: T): T {
    data[toIndex(location)] = value
    return value
  }

  private fun toIndex(location: Vector) = location.x + location.y * width

  private fun toLocation(index: Int) = Vector(index % width, index / width)

  fun forEachRowIndexed(function: (Int, List<T>) -> Unit) {
    (0 until height).forEach {
      function(it, getRow(it))
    }
  }

  fun <R> mapRowIndexed(function: (Int, List<T>) -> R): List<R> {
    return (0 until height).map {
      function(it, getRow(it))
    }
  }

  fun forEachColumnIndexed(function: (Int, List<T>) -> Unit) {
    (0 until width).forEach { col ->
      function(col, getColumn(col))
    }
  }

  fun getRow(row: Int): List<T> {
    val start = row * width
    return data.subList(start, start + width)
  }

  fun getColumn(col: Int): List<T> {
    return (0 until height).map { row ->
      this[Vector(col, row)]!!
    }
  }

  fun find(function: (Vector, T) -> Boolean): Pair<Vector, T>? {
    return data.withIndex().firstOrNull { (index, t) ->
      function(toLocation(index), t)
    }?.let { toLocation(it.index) to it.value }
  }

  fun getValue(location: Vector): T {
    return get(location) ?: throw Exception("Invalid location")
  }

  fun validLocation(Vector: Vector): Boolean  {
    return Vector.x < width && Vector.y < height && Vector.x >= 0 && Vector.y >= 0
  }
}

interface Grid<T> {
  fun coordsToIndex(x: Int, y: Int) : Int
  fun setIndex(index: Int, value: T)
  fun getIndex(index: Int): T
}

class CharGrid() : Grid<Char> {
  var width: Int = 0
  var height: Int = 0
  var grid : CharArray = CharArray(width * height)

  constructor(input: CharArray, width: Int, height: Int? = null) : this() {
    grid = input.clone()
    this.width = width
    this.height = height ?: (grid.size / width)
  }

  constructor(inputLines: List<String>) : this() {
    width = inputLines.first().length
    height = inputLines.size
    grid = inputLines.map(String::toList).flatten().toCharArray()
  }

  constructor(size: Int, default: Char = ' ') : this() {
    width = size
    height = size
    grid = CharArray(width * height)
    grid.fill(default)
  }

  constructor(width: Int, height: Int, default: Char = '.') : this() {
    this.width = width
    this.height = height
    grid = CharArray(width * height)
    grid.fill(default)
  }

  override fun toString(): String {
    val output = StringBuilder()
    output.append("$width, $height\n")
    grid.toList().windowed(width, width) {
      output.append(it.joinToString("")).append('\n')
    }

    return output.toString()
  }

  fun toStringWithHighlights(
    highlight: String = COLORS.LT_RED.toString(),
    predicate: (Char, Vector) -> Boolean,
  ): String {
    val output = StringBuilder()
    output.append("$width, $height\n")
    var row = 0
    grid.toList().windowed(width, width) {
      output.append(
        it.withIndex().joinToString("") { (index, c) ->
          val shouldHighlight = predicate(c, Vector(index, row))
          if (shouldHighlight) {
            highlight + c.toString() + NO_COLOR
          } else {
            c.toString()
          }
        }
      ).append('\n')
      row += 1
    }

    return output.toString()
  }

  fun toStringWithMultipleHighlights(
    vararg highlight: Pair<String, (Char, Vector) -> Boolean>
  ): String {
    val output = StringBuilder()
    output.append("$width, $height\n")
    var row = 0
    grid.toList().windowed(width, width) {
      output.append(
        it.withIndex().joinToString("") { (index, c) ->
          val hl = highlight.firstOrNull { it.second(c, Vector(index, row)) }
          if (hl != null) {
            hl.first + c.toString() + NO_COLOR
          } else {
            c.toString()
          }
        }
      ).append('\n')
      row += 1
    }

    return output.toString()
  }


  operator fun get(Vector: Vector): Char = getCell(Vector)

  fun findCharacter(target: Char): Vector? {
    val index = grid.indexOf(target)
    if (index == -1) {
      return null
    }
    return indexToVector(index)
  }

  fun indexToVector(index: Int): Vector {
    val y = index / width
    val x = index % width
    return Vector(x, y)
  }

  fun setCell(Vector: Vector, c: Char) {
    grid[vectorToIndex(Vector)] = c
  }

  fun vectorToIndex(Vector: Vector): Int {
    return Vector.x + Vector.y * width
  }

  fun getCell(Vector: Vector): Char {
    return grid[vectorToIndex(Vector)]
  }

  fun getCellOrNull(Vector: Vector): Char? {
    if (!validLocation(Vector)) {
      return null
    }
    return grid[vectorToIndex(Vector)]
  }

  fun getNeighbors(index: Int): List<Char> {
    val Vector = indexToVector(index)
    return getNeighbors(Vector)
  }

  fun getCell_xy(x: Int, y: Int): Char {
    return grid[coordsToIndex(x, y)]
  }

  override fun coordsToIndex(x: Int, y: Int): Int {
    return x + y * width
  }

  fun getNeighbors(vector: Vector): List<Char> {
    return Heading.values().mapNotNull { heading ->
      val v = vector + heading.vector
      if (validLocation(v)) {
        getCell(v)
      } else {
        null
      }
    }
  }

  fun getNeighborsWithLocation(vector: Vector): List<Pair<Vector, Char>> {
    return Heading.values().mapNotNull { heading ->
      val v = vector + heading.vector
      if (validLocation(v)) {
        v to getCell(v)
      } else {
        null
      }
    }
  }

  fun getNeighbor8sWithLocation(index: Int): List<Pair<Vector, Char>> {
    val vector = indexToVector(index)
    return Heading8.values().mapNotNull { heading ->
      val v = vector + heading.vector
      if (validLocation(v)) {
        v to getCell(v)
      } else {
        null
      }
    }
  }


  fun getNeighborsWithLocation(index: Int): List<Pair<Vector, Char>> =
    getNeighborsWithLocation(indexToVector(index))

  fun getNeighborsIf(Vector: Vector, predicate: (Char, Vector) -> Boolean): List<Vector> {
    return Heading.values().mapNotNull { heading->
      val loc = Vector + heading.vector
      if (validLocation(loc) && predicate(getCell(loc), loc)) {
        loc
      } else {
        null
      }
    }
  }

  override fun getIndex(index: Int): Char {
    return grid[index]
  }

  override fun setIndex(index: Int, value: Char) {
    grid[index] = value
  }

  fun initialize(input: String) {
    input.forEachIndexed{ i, c -> if (i < grid.size) grid[i] = c }
  }

  fun getNeighbors8(Vector: Vector, default: Char): List<Char> {
    return Heading8.values()
      .map { heading-> Vector + heading.vector }
      .map { neighborVector ->
        if (validLocation(neighborVector)) getCell(neighborVector) else default
      }
  }

  fun validLocation(Vector: Vector): Boolean  {
    return Vector.x < width && Vector.y < height && Vector.x >= 0 && Vector.y >= 0
  }

  fun copy(): CharGrid {
    return CharGrid(grid, width, height)
  }

  fun getNeighbors8(index: Int): List<Char> {
    val Vector = indexToVector(index)
    return Heading8.values()
      .map { heading-> Vector + heading.vector }
      .mapNotNull { neighborVector ->
        if (validLocation(neighborVector)) getCell(neighborVector) else null
      }
  }

  fun advance(action: (index: Int, Vector: Vector, Char) -> Char): CharGrid {
    val nextGrid = CharArray(width * height)
    for ((index, item) in grid.withIndex()) {
      val loc = indexToVector(index)
      nextGrid[index] = action(index, loc, item)
    }
    return CharGrid(nextGrid, width, height)
  }

  fun sameAs(other: CharGrid): Boolean {
    return grid contentEquals other.grid
  }

  fun getBorders(): List<Int> {
    val trans = mapOf('.' to '0', '#' to '1')

    return listOf(
      (0 until width).mapNotNull { trans[getCell_xy(it, 0)] },
      (0 until height).mapNotNull { trans[getCell_xy(width - 1, it)] },
      (0 until width).mapNotNull { trans[getCell_xy(it, height - 1)] },
      (0 until height).mapNotNull { trans[getCell_xy(0, it)] },
    ).flatMap {
      listOf(it, it.reversed())
    }.map { it.joinToString("") }
      .map { it.toInt(2) }
  }

  fun rotate(rotation: Int): CharGrid {
    var r = rotation
    var src = this
    if (r < 0) {
      while (r < 0) {
        val nextGrid = CharGrid(CharArray(width * height), width, height)
        (0 until height).forEach { row ->
          val rowContent = src.getRow(row).reversedArray()
          nextGrid.setColumn(row, rowContent)
        }
        r += 1
        src = nextGrid
      }
    } else {
      while (r > 0) {
        val nextGrid = CharGrid(CharArray(width * height), width, height)
        (0 until height).forEach { row ->
          val rowContent = src.getRow(row)
          nextGrid.setColumn((width - 1) - row, rowContent)
        }
        r -= 1
        src = nextGrid
      }
    }

    return src
  }

  private fun setColumn(col: Int, content: CharArray) {
    var index = col
    repeat(height) {
      grid[index] = content[it]
      index += width
    }
  }

  fun getRow(row: Int): CharArray {
    val start = row * width
    return grid.sliceArray(start until (start + width))
  }

  private fun setRow(row: Int, content: CharArray) {
    val start = row * width
    val end = start + width
    (start until end).forEachIndexed { index, it -> grid[it] = content[index] }
  }

  fun getBorder(heading: Heading) {
    val trans = mapOf('.' to '0', '#' to '1')

    val thing = when (heading) {
      Heading.NORTH -> (0 until width).mapNotNull { trans[getCell_xy(it, 0)] }
      Heading.EAST -> (0 until height).mapNotNull { trans[getCell_xy(width - 1, it)] }
      Heading.SOUTH -> (0 until width).mapNotNull { trans[getCell_xy(it, height - 1)] }
      Heading.WEST ->  (0 until height).mapNotNull { trans[getCell_xy(0, it)] }
    }.joinToString("")
    println(thing)
//      .map { it.toInt(2) }
  }

  fun flipAlongVerticalAxis(): CharGrid {
    var i = 0
    var j = width - 1
    val nextGrid = CharGrid(CharArray(width * height), width, height)
    while (i < j) {
      nextGrid.setColumn(j, getColumn(i))
      nextGrid.setColumn(i, getColumn(j))
      i++
      j--
    }
    if (i == j) {
      nextGrid.setColumn(i, getColumn(i))
    }
    return nextGrid
  }

  fun flipAlongHorizontalAxis(): CharGrid {
    var i = 0
    var j = height - 1
    val nextGrid = CharGrid(CharArray(width * height), width, height)
    while (i < j) {
      nextGrid.setRow(j, getRow(i))
      nextGrid.setRow(i, getRow(j))
      i++
      j--
    }
    if (i == j) {
      nextGrid.setRow(i, getRow(j))
    }
    return nextGrid
  }


  fun getColumn(col: Int): CharArray {
    val result = CharArray(height)
    var index = col
    repeat(height) {
      result[it] = grid[index]
      index += width
    }
    return result
  }

  fun stripBorder(): CharGrid {
    val nextWidth = width - 2
    val nextHeight = height - 2
    val nextGrid = CharGrid(CharArray(nextWidth * nextHeight), nextWidth, nextHeight)
    (1 .. nextHeight).forEach { row ->
      (1 .. nextHeight).forEach { col ->
        nextGrid.setCell_xy(col - 1, row - 1, getCell_xy(col, row))
      }
    }
    return nextGrid
  }

  fun setCell_xy(x: Int, y: Int, ch: Char) {
    grid[coordsToIndex(x, y)] = ch
  }

  fun getPermutations(): List<CharGrid> {
    return getFlips().flatMap {
      it.getRotations()
    }
  }

  private fun getFlips(): List<CharGrid> {
    return listOf(
      this,
      this.flipAlongHorizontalAxis(),
      this.flipAlongVerticalAxis(),
      this.flipAlongHorizontalAxis().flipAlongVerticalAxis()
    )
  }

  private fun getRotations(): List<CharGrid> {
    return (0..3).map {
      rotate(it)
    }
  }

  fun insertAt_xy(xOffset: Int, yOffset: Int, other: CharGrid) {
    repeat(other.height) { row ->
      val y = row + yOffset
      repeat(other.width) { col ->
        val x = col + xOffset
        setCell_xy(x, y, other.getCell_xy(col, row))
      }
    }
  }

  fun findAll(predicate: (Int, Char) -> Boolean): List<Pair<Int, Char>> {
    return grid.withIndex().filter { (index, value) ->
      predicate(index, value)
    }.map { (index, value) -> index to value }
  }

  fun pivot(): CharGrid {
    val outGrid = CharGrid(CharArray(width * height), height, width)

    repeat(height) { row ->
      repeat(width) { col ->
        outGrid.setCell_xy(row, col, getCell_xy(col, row))
      }
    }

    return outGrid
  }

  fun rotateClockwise(): CharGrid {
    val outGrid = CharGrid(CharArray(width * height), height, width)

    repeat(height) { row ->
      repeat(width) { col ->
        val outY = col
        val outX = (height - 1) - row
        val src = getCell_xy(col, row)
        outGrid.setCell_xy(outX, outY, src)
      }
    }

    return outGrid
  }

  fun rotateCounterClockwise(): CharGrid {
    val outGrid = CharGrid(CharArray(width * height), height, width)

    repeat(height) { row ->
      repeat(width) { col ->
        val outX = row
        val outY = (width - 1) - col
        val src = getCell_xy(col, row)
        outGrid.setCell_xy(outX, outY, src)
      }
    }

    return outGrid
  }

  operator fun set(it: Vector, value: Char) {
    this.setCell(it, value)
  }
}
