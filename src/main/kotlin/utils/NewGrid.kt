package utils

class NewGrid<T>(width: Int, height: Int, initialData : List<T>) {
  var data: MutableList<T> = initialData.toMutableList()

  var width: Int
    private set

  var height: Int
    private set

  constructor(width: Int, height: Int, value: T) :
    this(width, height, MutableList(width*height) { value }) {
  }

  init {
    this.width = width
    this.height = height
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


  fun getValidNeighbors(location: Vector) = location.neighbors().mapNotNull { neighborLocation ->
    this[neighborLocation]?.let { neighborValue ->
      neighborLocation to neighborValue
    }
  }

  fun getNeighbors8(location: Vector) = location.neighbors8().map { it to this[it] }

  fun getValidNeighbors8(location: Vector) = location.neighbors8().mapNotNull { neighborLocation ->
    this[neighborLocation]?.let { neighborValue ->
      neighborLocation to neighborValue
    }
  }

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

  operator fun get(index: Int): T? {
    return data[index]
  }

  operator fun get(x: Int, y: Int): T? {
    return data[toIndex(x, y)]
  }

  operator fun set(location: Vector, value: T): T {
    data[toIndex(location)] = value
    return value
  }

  operator fun set(index: Int, value: T): T {
    data[index] = value
    return value
  }

  operator fun set(x: Int, y: Int, value: T) {
    data[toIndex(x, y)] = value
  }

  fun toIndex(location: Vector) = location.x + location.y * width

  fun toIndex(x: Int, y: Int) = x + y * width

  private fun toLocation(index: Int) = Vector(index % width, index / width)

  fun forEachRowIndexed(function: (Int, List<T>) -> Unit) {
    (0 until height).forEach {
      function(it, getRow(it))
    }
  }

  fun filteredRowsIndexed(predicate: (Int, List<T>) -> Boolean): List<Pair<Int, List<T>>> {
    return buildList {
      (0 until height).forEach { row ->
        val data = getRow(row)
        if (predicate(row, data)) {
          add(row to data)
        }
      }
    }
  }

  fun <R> mapRowIndexed(function: (Int, List<T>) -> R): List<R> {
    return (0 until height).map {
      function(it, getRow(it))
    }
  }

  fun replaceRowsIndexed(function: (Int, List<T>) -> List<T>) {
    (0 until height).map {
      function(it, getRow(it))
    }.forEachIndexed { rowIndex, rowData ->
      rowData.forEachIndexed { colIndex, t ->
        set(colIndex, rowIndex, t)
      }
    }
  }

  fun <R> mapColumnIndexed(function: (Int, List<T>) -> R): List<R> {
    return (0 until width).map {
      function(it, getColumn(it))
    }
  }

  fun replaceColumnsIndexed(function: (Int, List<T>) -> List<T>) {
    (0 until width).map {
      function(it, getColumn(it))
    }.forEachIndexed { colIndex, columnData ->
      columnData.forEachIndexed { rowIndex, t ->
        set(colIndex, rowIndex, t)
      }
    }
  }

  fun <R> mapRows(function: (List<T>) -> R): List<R> {
    return (0 until height).map {
      function(getRow(it))
    }
  }

  fun forEachColumnIndexed(function: (Int, List<T>) -> Unit) {
    (0 until width).forEach { col ->
      function(col, getColumn(col))
    }
  }

  fun filteredColumnsIndexed(predicate: (Int, List<T>) -> Boolean): List<Pair<Int, List<T>>> {
    return buildList {
      (0 until width).forEach { column ->
        val data = getColumn(column)
        if (predicate(column, data)) {
          add(column to data)
        }
      }
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

  fun findAll(function: (Vector, T) -> Boolean): List<Pair<Vector, T>> {
    return data.withIndex().filter { (index, t) ->
      function(toLocation(index), t)
    }.map { toLocation(it.index) to it.value }
  }

  fun getValue(location: Vector): T {
    return get(location) ?: throw Exception("Invalid location")
  }

  fun validLocation(vector: Vector): Boolean  {
    return vector.x < width && vector.y < height && vector.x >= 0 && vector.y >= 0
  }

  override fun toString(): String = mapRows { it.joinToString("") }.joinToString("\n")

  fun subGrid(vector: Vector, width: Int, height: Int): NewGrid<T>? {
    return if (((vector.x + width) <= this.width) && ((vector.y + height) <= this.height)) {
      val values = (vector.y until (vector.y + height)).flatMap { y ->
        (vector.x until (vector.x + width)).map { x ->
          get(x, y)!!
        }
      }
      NewGrid(width, height, values.toMutableList())
    } else {
      null
    }
  }

  fun makeCopy(): NewGrid<T> {
    return NewGrid(width, height, data)
  }

  fun toStringWithHighlights(
    highlight: String = COLORS.LT_RED.toString(),
    predicate: (T, Vector) -> Boolean,
  ): String {
    val output = StringBuilder()
    output.append("$width, $height\n")
    var row = 0
    data.toList().windowed(width, width) {
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

  fun toStringWithHighlights(vararg highlights: Pair<String, (T, Vector) -> Boolean>): String {
    val output = StringBuilder()
    output.append("$width, $height\n")
    var row = 0

    data.toList().windowed(width, width) {
      output.append(
        it.withIndex().joinToString("") { (index, c) ->
          highlights.firstOrNull { cell -> cell.second(c, Vector(index, row)) }?.let { (color, _) ->
            color + c.toString() + NO_COLOR
          } ?: c.toString()
        }
      ).append('\n')
      row += 1
    }

    return output.toString()
  }

  fun addRow(rowIndex: Int, t: T) {
    data.addAll(toIndex(0, rowIndex), List(width) { t })
    height += 1
  }

  fun replaceRow(rowIndex: Int, t: T) {
    (toIndex(0, rowIndex) until toIndex(width, rowIndex)).forEach {
      data[it] = t
    }
  }

  fun addColumn(colIndex: Int, t: T) {
    (0 until height).reversed().forEach { row ->
      data.add(toIndex(colIndex, row), t)
    }
    width += 1
  }

  fun replaceColumn(colIndex: Int, t: T) {
    (0 until height).reversed().forEach { row ->
      this[Vector(colIndex, row)] = t
    }
  }

  companion object {
    fun fromCollectionOfStrings(input: Collection<String>): NewGrid<Char> {
      val value = input.joinToString("").toList()
      return NewGrid(input.first().length, input.size, value)
    }
  }
}