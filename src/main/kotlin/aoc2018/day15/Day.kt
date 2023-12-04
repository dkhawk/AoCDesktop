package aoc2018.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.NewGrid
import utils.Vector
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  var part = 0
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf(500L)
  val maxDelay = 500L

  val sampleInput0 = """
    #########
    #G..G..G#
    #.......#
    #.......#
    #G..E..G#
    #.......#
    #.......#
    #G..G..G#
    #########
  """.trimIndent().split("\n")

  val sampleInput = """
    #######
    #.G...#
    #...EG#
    #.#.#G#
    #..G#E#
    #.....#
    #######
  """.trimIndent().split("\n")

  val sampleInput1 = """
    #######       #######
    #G..#E#       #...#E#   E(200)
    #E#E.E#       #E#...#   E(197)
    #G.##.#  -->  #.E##.#   E(185)
    #...#E#       #E..#E#   E(200), E(200)
    #...E.#       #.....#
    #######       #######
  """.trimIndent().split("\n")

  val sampleInput2 = """
    #######       #######   
    #E..EG#       #.E.E.#   E(164), E(197)
    #.#G.E#       #.#E..#   E(200)
    #E.##E#  -->  #E.##.#   E(98)
    #G..#.#       #.E.#.#   E(200)
    #..E#.#       #...#.#   
    #######       #######   
  """.trimIndent().split("\n")

  val sampleInput3 = """
    #######       #######
    #.G...#       #..E..#   E(158)
    #...EG#       #...E.#   E(14)
    #.#.#G#  -->  #.#.#.#
    #..G#E#       #...#.#
    #.....#       #.....#
    #######       #######
  """.trimIndent().split("\n")

  val sampleInput4 = """
    #######       #######
    #E..EG#       #.E.E.#   E(200), E(23)
    #.#G.E#       #.#E..#   E(200)
    #E.##E#  -->  #E.##E#   E(125), E(200)
    #G..#.#       #.E.#.#   E(200)
    #..E#.#       #...#.#
    #######       #######
  """.trimIndent().split("\n")

  val sampleInput5 = """
    #######       #######
    #E.G#.#       #.E.#.#   E(8)
    #.#G..#       #.#E..#   E(86)
    #G.#.G#  -->  #..#..#
    #G..#.#       #...#.#
    #...E.#       #.....#
    #######       #######
  """.trimIndent().split("\n")

  val sampleInput6 = """
    #########       #########   
    #G......#       #.......#   
    #.E.#...#       #.E.#...#   E(38)
    #..##..G#       #..##...#   
    #...##..#  -->  #...##..#   
    #...#...#       #...#...#   
    #.G...G.#       #.......#   
    #.....G.#       #.......#   
    #########       #########   
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

  sealed class Player(var location: Vector) {
    abstract val symbol: Char
    private var hitPoints: Int = 200

    fun isDead() = hitPoints <= 0

    class Elf(location: Vector, private val power: Int = 3) : Player(location) {
      override val symbol: Char
        get() = 'E'

      override fun type() = "Elf"
      override fun attackPower() = power

      override fun opponentSymbol() = 'G'
    }

    class Goblin(location: Vector) : Player(location) {
      override val symbol: Char
        get() = 'G'

      override fun type() = "Goblin"
      override fun opponentSymbol() = 'E'
      override fun attackPower() = 3
    }

    override fun toString(): String {
      return "${type()} at $location ($hitPoints)"
    }

    abstract fun type(): String
    fun isAlive() = !isDead()

    fun getHitPoints() = hitPoints
    abstract fun attackPower(): Int
    fun takeDamage(points: Int) {
      hitPoints -= points
    }

    abstract fun opponentSymbol(): Char
  }

  fun part1() {
    // input = sampleInput2.map { it.take(7) }
    val grid = NewGrid.fromCollectionOfStrings(input)

    var players = grid.findAll { _, c -> c == 'E' || c == 'G' }.map { (location, c) ->
      when (c) {
        'E' -> Player.Elf(location)
        'G' -> Player.Goblin(location)
        else -> throw Exception("Unexpected character $c")
      }
    }

    var round = 0

    val interestingRounds = emptyList<Int>() // listOf(1,2,23,24,25,28,47)

    while (true) {
      // Check for a winner

      players.forEach { player ->
        if (!player.isDead()) {
          val opponents =
            players.filter { other -> other::class != player::class && other.isAlive() }
          val opponentsAdjacentSpaces = opponents.map { it to grid.getValidNeighbors(it.location) }

          // attack first in range opponent
          var inRangeOpponents = grid.getValidNeighbors(player.location)
            .filter { (_, c) -> c == player.opponentSymbol() }
            .map { (l, _) ->
              players.first { l == it.location }
            }

          // var inRangeOpponents = opponentsAdjacentSpaces.filter { (opponent, adjacentSpaces) ->
          //   adjacentSpaces.map { it.first }.contains(player.location)
          // }.map { it.first }

          if (inRangeOpponents.isEmpty()) {
            val openAttackSpaces = opponentsAdjacentSpaces.flatMap { (player, spaces) ->
              spaces.filter { it.second == '.' }.map { it.first }
            }.sortedWith(compareBy<Vector> { it.y }.thenBy { it.x })
            // look for in range enemy and move
            // Depth first search.  Stop when we hit the first open location.
            // Do the search in reading order!
            val path = breadFirstSearch(grid, player.location, openAttackSpaces)
            if (path.isNotEmpty()) {
              // Update location in grid
              grid[player.location] = '.'
              player.location = path.first()
              grid[player.location] = player.symbol
            }

            inRangeOpponents = grid.getValidNeighbors(player.location)
              .filter { (_, c) -> c == player.opponentSymbol() }
              .map { (l, _) ->
                players.first { l == it.location }
              }
          }

          if (inRangeOpponents.isNotEmpty()) {
            // Attack!!!
            inRangeOpponents.minByOrNull { it.getHitPoints() }?.let { weakest ->
              weakest.takeDamage(player.attackPower())
              if (weakest.isDead()) {
                grid[weakest.location] = '.'
              }
            }
          }
        }
      }

      players = players.filter { it.isAlive() }.sortedWith(
        compareBy<Player> { it.location.y }.thenBy { it.location.x }
      )

      if (
        players.none { it.isAlive() && it is Player.Elf } ||
        players.none { it.isAlive() && it is Player.Goblin }
      ) {
        println("Game over, man!  Game over!")
        break
      }

      round++

      if (round in interestingRounds) {
        println("Round $round")
        println(grid)
        println(players.joinToString("\n"))
        println()
      }
    }

    println("Round $round")
    println(grid)
    println(players.joinToString("\n"))
    println()

    val hp = players.sumOf { it.getHitPoints() }
    val answer = round * hp
    println(round)
    println(hp)
    println(answer)
  }

  data class GameResult(
    val score: Int,
    val players: List<Player>,
    val lastRound: Int,
    val elfDeath: Boolean
  )

  fun part2() {
    // val sample = sampleInput4
    // val spaceIndex = sample.first().indexOfFirst { it == ' ' }
    // input = sample.map { it.take(spaceIndex) }

    var maxElfPower = 200
    var minElfPower = 4

    val gameResults = mutableMapOf<Int, GameResult>()

    while (minElfPower < maxElfPower) {
      val elfPower = (maxElfPower + minElfPower) / 2

      var result = gameResults[elfPower]
      if (result == null) {
        result = playGame(elfPower)
      }

      val elfDeath = result.elfDeath

      if (elfDeath) {
        // Need more power
        minElfPower = elfPower.coerceAtLeast(minElfPower + 1)
      } else {
        maxElfPower = (elfPower + maxElfPower) / 2
      }
      println("elf death = $elfDeath at $elfPower.  Updated $minElfPower  $maxElfPower")

      if (minElfPower >= maxElfPower) {
        if (elfDeath) {
          result = gameResults.getOrPut(maxElfPower) { playGame(maxElfPower) }
        }
        println(result.players.joinToString("\n"))
        val hp = result.players.sumOf { it.getHitPoints() }
        val answer = result.lastRound * hp
        println("answer: ${result.lastRound} * $hp => $answer")
        println("or answer: ${result.lastRound + 1} * $hp => ${(result.lastRound + 1) * hp}")
        break
      }
    }

    println(minElfPower)
    println(maxElfPower)

  }

  private fun playGame(elfPower: Int): GameResult {
    val grid = NewGrid.fromCollectionOfStrings(input)

    println("elfPower $elfPower")

    var players = grid.findAll { _, c -> c == 'E' || c == 'G' }.map { (location, c) ->
      when (c) {
        'E' -> Player.Elf(location, elfPower)
        'G' -> Player.Goblin(location)
        else -> throw Exception("Unexpected character $c")
      }
    }

    var round = 0

    var elfDeath = false
    var gameOver = false
    var fullRound = false

    while (!elfDeath && !gameOver) {
      for (player in players) {
        if (player == players.last()) {
          fullRound = true
        }
        if (!player.isDead()) {
          val opponents =
            players.filter { other -> other::class != player::class && other.isAlive() }
          val opponentsAdjacentSpaces =
            opponents.map { it to grid.getValidNeighbors(it.location) }

          // attack first in range opponent
          var inRangeOpponents = grid.getValidNeighbors(player.location)
            .filter { (_, c) -> c == player.opponentSymbol() }
            .map { (l, _) ->
              players.first { l == it.location }
            }

          // var inRangeOpponents = opponentsAdjacentSpaces.filter { (opponent, adjacentSpaces) ->
          //   adjacentSpaces.map { it.first }.contains(player.location)
          // }.map { it.first }

          if (inRangeOpponents.isEmpty()) {
            val openAttackSpaces = opponentsAdjacentSpaces.flatMap { (player, spaces) ->
              spaces.filter { it.second == '.' }.map { it.first }
            }.sortedWith(compareBy<Vector> { it.y }.thenBy { it.x })
            // look for in range enemy and move
            // Depth first search.  Stop when we hit the first open location.
            // Do the search in reading order!
            val path = breadFirstSearch(grid, player.location, openAttackSpaces)
            if (path.isNotEmpty()) {
              // Update location in grid
              grid[player.location] = '.'
              player.location = path.first()
              grid[player.location] = player.symbol
            }

            inRangeOpponents = grid.getValidNeighbors(player.location)
              .filter { (_, c) -> c == player.opponentSymbol() }
              .map { (l, _) ->
                players.first { l == it.location }
              }
          }

          if (inRangeOpponents.isNotEmpty()) {
            // Attack!!!
            inRangeOpponents.minByOrNull { it.getHitPoints() }?.let { weakest ->
              weakest.takeDamage(player.attackPower())
              if (weakest.isDead()) {
                if (weakest is Player.Elf) {
                  elfDeath = true
                }
                grid[weakest.location] = '.'
              }
            }
            if (players.none { it.isAlive() && it is Player.Goblin } ) {
              gameOver = true
            }
          }
        }
      }
      players = players.filter { it.isAlive() }.sortedWith(
        compareBy<Player> { it.location.y }.thenBy { it.location.x }
      )

      if (fullRound) round += 1
    }

    val score = players.sumOf { it.getHitPoints().coerceAtLeast(0) } * round
    return GameResult(score, players, round, elfDeath)
  }

  private fun breadFirstSearch(
    grid: NewGrid<Char>,
    start: Vector,
    goals: List<Vector>,
  ): List<Vector> {
    val explored = mutableSetOf<Vector>()
    val queue = ArrayDeque<Vector>(1000)
    val cameFrom = mutableMapOf<Vector, Vector>()

    var target: Vector? = null
    queue.add(start)

    while (queue.isNotEmpty()) {
      val location = queue.removeFirst()

      if (goals.contains(location)) {
        target = location
        break
      }

      explored.add(location)

      val nextLocations = grid.getValidNeighbors(location)
        .filter { (_, c) -> c == '.' }
        .map { it.first }
        .filterNot { explored.contains(it) }

      nextLocations.forEach { nl ->
        if (!cameFrom.contains(nl)) {
          cameFrom[nl] = location
          queue.addLast(nl)
        }
      }
    }

    val path = mutableListOf<Vector>()
    while (target != null && target != start) {
      path.add(target)
      target = cameFrom[target]
    }

    return path.reversed()
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
