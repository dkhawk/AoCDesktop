package aoc2023.day16

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import utils.Heading
import utils.InputNew
import utils.NewGrid
import utils.Vector
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    .|...\....
    |.-.\.....
    .....|-...
    ........|.
    ..........
    .........\
    ..../.\\..
    .-.-/..|..
    .|....-|.\
    ..//.|....
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  data class Beam(val location: Vector, val heading: Heading) {
    fun advance() = copy(location = location.advance(heading))

    fun reflect(mirror: Char): Beam {
      val newHeading = when (mirror) {
        '/' -> when (heading) {
          Heading.NORTH -> Heading.EAST
          Heading.WEST -> Heading.SOUTH
          Heading.EAST -> Heading.NORTH
          Heading.SOUTH -> Heading.WEST
        }
        '\\' -> when (heading) {
          Heading.NORTH -> Heading.WEST
          Heading.WEST -> Heading.NORTH
          Heading.EAST -> Heading.SOUTH
          Heading.SOUTH -> Heading.EAST
        }
        else -> throw Exception("Unknown reflection character $mirror")
      }

      return copy(heading = newHeading)
    }

    fun split(splitter: Char): Collection<Beam> {
      if (splitter !in listOf('-', '|')) throw Exception("Unknown splitter character $splitter")
      return buildList<Beam> {
        when (heading) {
          Heading.NORTH, Heading.SOUTH -> if (splitter == '|') {
            add(this@Beam)
          } else {
            add(copy(heading = Heading.WEST))
            add(copy(heading = Heading.EAST))
          }
          Heading.WEST, Heading.EAST -> if (splitter == '-') {
            add(this@Beam)
          } else {
            add(copy(heading = Heading.NORTH))
            add(copy(heading = Heading.SOUTH))
          }
        }
      }
    }
  }

  fun part1() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val beams = ArrayDeque<Beam>()
    beams.addLast(Beam(Vector(0, 0), Heading.EAST))

    val heatedTiles = mutableSetOf<Vector>()

    val visited = mutableSetOf<Beam>()

    while (beams.isNotEmpty()) {
      val beam = beams.removeFirst()
      visited.add(beam)
      heatedTiles.add(beam.location)

      buildList {
        when (val c = grid[beam.location]!!) {
          '.' -> add(beam)
          '/', '\\' -> add(beam.reflect(c))
          '-', '|' -> addAll(beam.split(c))
        }
      }.map { it.advance() }
        .filter { grid.validLocation(it.location) }
        .filter { !visited.contains(it) }
        .forEach { beams.add(it) }
    }

    // println(grid)

    heatedTiles.forEach { grid[it] = '#' }

    // visited.forEach { beam ->
    //   val c = grid[beam.location]
    //
    //   when {
    //     c == null -> {}
    //     c in listOf('<', '>', '^', 'v', ) -> grid[beam.location] = '2'
    //     c == '*' -> {}
    //     c == '9' -> grid[beam.location] = '*'
    //     c.isDigit() -> grid[beam.location] = c + 1
    //     c == '.' -> grid[beam.location] = beam.heading.toSymbol()
    //     else -> {}
    //   }
    // }

    // println()

    // println(grid)

    println(heatedTiles.size)
    // 6751 is too low!
    // Ugh.  Had South hitting / going East!  FFFUUUU!!!
  }

  lateinit var g: NewGrid<Char>

  fun part2() {
    val grid = NewGrid.fromCollectionOfStrings(input)

    val startingLocations = buildList {
      (0 until grid.width).map { col ->
        add(Beam(Vector(col, 0), Heading.SOUTH))
        add(Beam(Vector(col, grid.height - 1), Heading.NORTH))
      }
      (0 until grid.height).map { row ->
        add(Beam(Vector(0, row), Heading.EAST))
        add(Beam(Vector(grid.width - 1, row), Heading.WEST))
      }
    }

    g = grid

    // val max = startingLocations.maxOf { heatTiles(it) }

    // This works, but is actually just a bit slower? :/
    val max = runBlocking {
      withContext(Dispatchers.Default) {
        startingLocations.map { async { heatTiles(it) } }.awaitAll().maxOf { it }
      }
    }

    println(max)
  }

  private fun heatTiles(start: Beam): Int {
    val beams = ArrayDeque<Beam>()
    beams.addLast(start)

    val heatedTiles = mutableSetOf<Vector>()

    val visited = mutableSetOf<Beam>()

    while (beams.isNotEmpty()) {
      val beam = beams.removeFirst()
      visited.add(beam)
      heatedTiles.add(beam.location)

      buildList {
        when (val c = g[beam.location]!!) {
          '.' -> add(beam)
          '/', '\\' -> add(beam.reflect(c))
          '-', '|' -> addAll(beam.split(c))
        }
      }.map { it.advance() }
        .filter { g.validLocation(it.location) }
        .filter { !visited.contains(it) }
        .forEach { beams.add(it) }
    }

    return heatedTiles.size
  }

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
}

private fun Heading.toSymbol() = when (this) {
  Heading.NORTH -> '^'
  Heading.WEST -> '<'
  Heading.EAST -> '>'
  Heading.SOUTH -> 'v'
}
