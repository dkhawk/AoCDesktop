package aoc2018.day15

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.io.File
import java.util.PriorityQueue
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

  data class GameSummary(val power: Int, val rounds: Int, val hp: Int, val score: Int)

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
  val expected3 = GameSummary(power = 15, rounds = 29, hp = 172, score = 4988)

  data class TestCase(val sampleInput: List<String>, val expected: GameSummary)


  val sampleInput4 = """
    #######       #######
    #E..EG#       #.E.E.#   E(200), E(23)
    #.#G.E#       #.#E..#   E(200)
    #E.##E#  -->  #E.##E#   E(125), E(200)
    #G..#.#       #.E.#.#   E(200)
    #..E#.#       #...#.#
    #######       #######
  """.trimIndent().split("\n")
  val expected4 = GameSummary(power = 4, rounds = 33, hp = 948, score = 31284)


  val sampleInput5 = """
    #######       #######
    #E.G#.#       #.E.#.#   E(8)
    #.#G..#       #.#E..#   E(86)
    #G.#.G#  -->  #..#..#
    #G..#.#       #...#.#
    #...E.#       #.....#
    #######       #######
  """.trimIndent().split("\n")
  val expected5 = GameSummary(power = 15, rounds = 37, hp = 94, score = 3478)

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
  val expected6 = GameSummary(power = 34, rounds = 30, hp = 38, score = 1140)

  val sampleInput7 = """
    #######       #######
    #.E...#       #...E.#   E(14)
    #.#..G#       #.#..E#   E(152)
    #.###.#  -->  #.###.#
    #E#G#G#       #.#.#.#
    #...#G#       #...#.#
    #######       #######
  """.trimIndent().split("\n")
  val expected7 = GameSummary(power = 12, rounds = 39, hp = 166, score = 6474)

  val sampleInput8 = """
    #######       #######
    #E.G#.#       #.E.#.#   E(8)
    #.#G..#       #.#E..#   E(86)
    #G.#.G#  -->  #..#..#
    #G..#.#       #...#.#
    #...E.#       #.....#
    #######       #######
  """.trimIndent().split("\n")
  val expected8 = GameSummary(power = 15, rounds = 37, hp = 94, score = 3478)

  val testCases = mutableListOf(
    TestCase(sampleInput3, expected3),
    TestCase(sampleInput4, expected4),
    TestCase(sampleInput5, expected5),
    TestCase(sampleInput6, expected6),
    TestCase(sampleInput7, expected7),
    TestCase(sampleInput8, expected8),
  )

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
        // println("Game over, man!  Game over!")
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

  /*
  I felt that the fact that we need to use the round number from the last completed full round
  doesn't make sense. It should have been the current round number as that was what was most
  intuitive. I spent a good 30 minutes trying to fix the off by 1 issue with the round only to
  realise that as I didn't think to look for that. The first example given is one where the last
  goblin that dies happens to die at the end of the round, so my initial approach worked for that,
  but it didn't work for the second example or my input. You can see in my gist the awkward code to
  get that edge case working.
   */

  /*
  The round does not finish if there are no enemies left to fight
   */

  fun part2() {
    // part2Tests()
    part2Real()
    // Day15.test()

    /*
    Elf at 19, 21 (59)
Elf at 21, 12 (191)
Elf at 8, 18 (179)
Elf at 13, 20 (200)
Elf at 14, 11 (17)
Elf at 10, 18 (200)
Elf at 11, 12 (29)
Elf at 12, 11 (41)
Elf at 4, 15 (200)
Elf at 10, 16 (119)
Finished 50 full rounds
Remaining hitpoints: 1235
Outcome: 61750
All elves survived with attack power of 17

     */
  }

  fun part2Real(): Pair<Int, GameResult> {
    val grid = NewGrid.fromCollectionOfStrings(input)
    println(grid)

    var players = grid.findAll { _, c -> c == 'E' || c == 'G' }.map { (location, c) ->
      when (c) {
        'E' -> Player.Elf(location, 50)
        'G' -> Player.Goblin(location)
        else -> throw Exception("Unexpected character $c")
      }
    }

    println(players.joinToString("\n"))

    var maxElfPower = 200
    var minElfPower = 4

    val gameResults = mutableMapOf<Int, GameResult>()

    while (minElfPower < maxElfPower) {
      val elfPower = (maxElfPower + minElfPower) / 2
      // println("Begin $minElfPower $elfPower $maxElfPower")
      val result = gameResults.getOrPut(elfPower) { playGame(elfPower) }

      // println(result)

      if (result.elfDeath) {
        // println("Death")
        minElfPower = elfPower + 1
      } else {
        // println("Victory")
        maxElfPower = elfPower
      }

      // println("End $minElfPower $elfPower $maxElfPower")
      // println()
    }

    val minResult = gameResults.getOrPut(minElfPower) { playGame(minElfPower) }
    val maxResult = gameResults.getOrPut(maxElfPower) { playGame(maxElfPower) }

    val (elfPower, bestResult) = if (minResult.elfDeath) maxElfPower to maxResult else minElfPower to minResult

    val hp = bestResult.players.sumOf { it.getHitPoints() }

    println(bestResult.players.joinToString("\n"))

    println("Elf power: $elfPower")
    val answer = bestResult.lastRound * hp
    println("answer: ${bestResult.lastRound} * $hp => $answer")
    println("or answer: ${bestResult.lastRound + 1} * $hp => ${(bestResult.lastRound + 1) * hp}")

    return elfPower to bestResult
  }

  fun part2Tests() {
    testCases.forEachIndexed { index, testCase ->
      println("=======================================")
      println(index)
      val tc: TestCase? = testCase
      var expected: GameSummary? = null

      if (tc != null) {
        val sample = tc.sampleInput
        expected = tc.expected

        val spaceIndex = sample.first().indexOfFirst { it == ' ' }
        input = sample.map { it.take(spaceIndex) }
      }

      val (elfPower, bestResult) = part2Real()

      val hp = bestResult.players.sumOf { it.getHitPoints() }

      if (expected != null) {
        val actual = GameSummary(
          power = elfPower,
          rounds = bestResult.lastRound,
          hp = hp,
          score = hp * bestResult.lastRound
        )

        if (actual == expected) {
          println("Got expected result: $expected")
        } else {
          println("Actual: $actual, Expected: $expected")
        }
      }

    }
  }

  private fun playGame(elfPower: Int): GameResult {
    val grid = NewGrid.fromCollectionOfStrings(input)

    // println("elfPower $elfPower")

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
    var roundComplete = false
    var everybodyHadATurn = false

    gameLoop@ while (!elfDeath && !gameOver) {
      for (index in players.indices) {
        val player = players[index]
        if (!player.isDead()) {
          val opponents =
            players.filter { other -> other::class != player::class && other.isAlive() }

          if (opponents.isEmpty()) break@gameLoop

          val opponentsAdjacentSpaces =
            opponents.map { it to grid.getValidNeighbors(it.location) }

          // attack first in range opponent
          var inRangeOpponents = grid.getValidNeighbors(player.location)
            .filter { (_, c) -> c == player.opponentSymbol() }
            .map { (l, _) ->
              players.first { l == it.location }
            }

          if (inRangeOpponents.isEmpty()) {
            inRangeOpponents =
              moveAndRecompute(opponentsAdjacentSpaces, grid, player, inRangeOpponents, players)
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
          }
        }
      }

      players = players.filter { it.isAlive() }.sortedWith(
        compareBy<Player> { it.location.y }.thenBy { it.location.x }
      )

      round += 1

      if (players.none { it.isAlive() && it is Player.Goblin } ||
        players.none { it.isAlive() && it is Player.Elf }) {
        gameOver = true
      }
    }

    players = players.filter { it.isAlive() }.sortedWith(
      compareBy<Player> { it.location.y }.thenBy { it.location.x }
    )

    val score = players.sumOf { it.getHitPoints().coerceAtLeast(0) } * round
    return GameResult(score, players, round, elfDeath)
  }

  private fun moveAndRecompute(
    opponentsAdjacentSpaces: List<Pair<Player, List<Pair<Vector, Char>>>>,
    grid: NewGrid<Char>,
    player: Player,
    inRangeOpponents: List<Player>,
    players: List<Player>,
  ): List<Player> {
    var inRangeOpponents1 = inRangeOpponents
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

    inRangeOpponents1 = grid.getValidNeighbors(player.location)
      .filter { (_, c) -> c == player.opponentSymbol() }
      .map { (l, _) ->
        players.first { l == it.location }
      }
    return inRangeOpponents1
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


class Day15 {
  companion object {
    fun test() {
      val d = Day15()
      d.part1()
    }
    /*
    * Combat ends after 37 full rounds
    Elves win with 982 total hit points left
    Outcome: 37 * 982 = 36334
     */
    val input1 = """
            #######
            #.G...#
            #...EG#
            #.#.#G#
            #..G#E#
            #.....#
            #######""".trimIndent()

    /*
    Combat ends after 46 full rounds
Elves win with 859 total hit points left
Outcome: 46 * 859 = 39514
     */
    val input2 = """
            #######
            #G..#E#
            #E#E.E#
            #G.##.#
            #...#E#
            #...E.#
            #######""".trimIndent()

    /*
    Combat ends after 35 full rounds
Goblins win with 793 total hit points left
Outcome: 35 * 793 = 27755
     */
    val input3 = """
            #######
            #E..EG#
            #.#G.E#
            #E.##E#
            #G..#.#
            #..E#.#
            #######""".trimIndent()

    val part2Input3 = """
            #######
            #E.G#.#
            #.#G..#
            #G.#.G#
            #G..#.#
            #...E.#
            #######""".trimIndent()

    /*
    Combat ends after 54 full rounds
Goblins win with 536 total hit points left
Outcome: 54 * 536 = 28944
     */
    val input4 = """
            #######
            #.E...#
            #.#..G#
            #.###.#
            #E#G#G#
            #...#G#
            #######""".trimIndent()

    /*
    Combat ends after 20 full rounds
Goblins win with 937 total hit points left
Outcome: 20 * 937 = 18740
        #########       #########
        #G......#       #.G.....#   G(137)
        #.E.#...#       #G.G#...#   G(200), G(200)
        #..##..G#       #.G##...#   G(200)
        #...##..#  -->  #...##..#
        #...#...#       #.G.#...#   G(200)
        #.G...G.#       #.......#
        #.....G.#       #.......#
        #########       #########

     */
    val input5 = """
            #########
            #G......#
            #.E.#...#
            #..##..G#
            #...##..#
            #...#...#
            #.G...G.#
            #.....G.#
            #########""".trimIndent()

    val part2input1 = """
            #######
            #.G...#
            #...EG#
            #.#.#G#
            #..G#E#
            #.....#
            #######""".trimIndent()
  }

  abstract class Creature(var x: Int, var y: Int) {
    companion object {
      var nextId = 0
    }

    var id = nextId++
    var hitPoints = 200
    var dead = false

    fun moveTo(state: CharArray, width: Int, x: Int, y: Int) {
//            println("$this moved to $x, $y")
      state[this.x + this.y * width] = '.'
      state[x + y * width] = getSymbol()
      this.x = x
      this.y = y
    }

    abstract fun getSymbol(): Char
    abstract fun getAttackPower(): Int

    fun attack(other: Creature): Boolean {
      other.hitPoints -= getAttackPower()
      return other.hitPoints <= 0
    }

    fun die(state: CharArray, width: Int, creatures: java.util.ArrayList<Creature>) {
      state[x + y * width] = '.'
      creatures.remove(this)
      dead = true
    }
  }

  class Elf(x: Int, y: Int) : Creature(x, y) {
    companion object {
      var attackPower = 3
    }
    override fun getSymbol(): Char {
      return 'E'
    }

    override fun getAttackPower(): Int {
      return attackPower
    }

    override fun toString(): String {
      return "Elf at $x, $y ($hitPoints)"
    }
  }

  class Goblin(x: Int, y: Int) : Creature(x, y) {
    override fun getSymbol(): Char {
      return 'G'
    }

    override fun getAttackPower(): Int {
      return 3
    }

    override fun toString(): String {
      return "Goblin at $x, $y ($hitPoints)"
    }
  }

  data class Target(val creatureId: Int, val x: Int, val y: Int, val hitPoints: Int, val dist: Int) {
    constructor(creature: Creature, dist: Int) : this(creature.id, creature.x, creature.y, creature.hitPoints, dist)
  }

  private fun part1() {
    // val input = input5
    val input = File("/Users/dkhawk/IdeaProjects/aocDesktop/src/main/resources/2018/15.txt").readText()
    val width = input.indexOfFirst { it == '\n' }

    var anElfDied = true

    while (anElfDied) {
      val state = input.filterNot { it == '\n' }.toCharArray()
      val creatures = ArrayList<Creature>()

      // Find all goblins and elves
      state.forEachIndexed { index, c ->
        val x = index % width
        val y = index / width

        when (c) {
          'E' -> Elf(x, y)
          'G' -> Goblin(x, y)
          else -> null
        }?.let { creatures.add(it) }
      }

      val numberOfElves = creatures.filterIsInstance<Elf>().size
      println("========= There are $numberOfElves elves =========")
      Elf.attackPower++
      var roundNumber = 0
      var gameOver = false

      while (creatures.find { it is Elf } != null && creatures.find { it is Goblin } != null) {
//                println("Round: $roundNumber")
//            creatures.forEach { println(it) }
//            printState(state, width)
        printStateWithStats(state, width, creatures)

//            println("========")

        val round = PriorityQueue<Creature>(creatures.size, compareBy(Creature::y, Creature::x))
        creatures.forEach { round.add(it) }

        while (round.isNotEmpty()) {
          val creature = round.poll()
//                    println("It's ${creature}'s turn")
          if (creature.dead) {
//                        println("$creature is dead!!!")
            continue
          }
//                println("Creature: $creature's turn")
          val enemies = creatures.filterNot { it::class == creature::class }
          if (enemies.isEmpty()) {
            println("Game over, man.  Game over!!")
            gameOver = true
            break
          }

//                enemies.forEach { println(it) }

          var potentialMap = createPotentialMap(creature, state, width)

//                printPotentialMap(potentialMap, width)

          val attackQueue =
            PriorityQueue<Target>(enemies.size, compareBy(Target::hitPoints, Target::y, Target::x))
          val moveQueue = PriorityQueue<Target>(enemies.size, compareBy(Target::dist, Target::y, Target::x))
          enemies.forEach { enemy ->
            // is the enemy in attack range?  Need to attack the enemy in range with the fewest remaining hit points
            val index = enemy.x + enemy.y * width
            when (val dist = potentialMap[index]) {
              1 -> attackQueue.add(Target(enemy, dist))
              Int.MAX_VALUE, 0 -> {
              }
              else -> moveQueue.add(Target(enemy, dist))
            }
          }

          if (attackQueue.isNotEmpty()) {
            if (attack(attackQueue, creatures, creature, state, width)) {
              println("An elf has died!!!")
              gameOver = true
              break
            }
            continue
          }

          if (moveQueue.isNotEmpty()) {
            val target = moveQueue.poll()
//                    println("Moving towards enemy: $target")
            val enemyTarget = findCreatureById(creatures, target.creatureId)!!

            val targetPotentialMap = createPotentialMap(enemyTarget, state, width)
//                    printPotentialMap(targetPotentialMap, width)

            val pq =
              PriorityQueue<PotentialPoint>(
                compareBy(
                  PotentialPoint::dist,
                  PotentialPoint::y,
                  PotentialPoint::x
                )
              )
            createAdjacentPoint(state, targetPotentialMap, creature, 0, -1, width)?.let { pq.add(it) }
            createAdjacentPoint(state, targetPotentialMap, creature, 0, 1, width)?.let { pq.add(it) }
            createAdjacentPoint(state, targetPotentialMap, creature, -1, 0, width)?.let { pq.add(it) }
            createAdjacentPoint(state, targetPotentialMap, creature, 1, 0, width)?.let { pq.add(it) }

            val goalLocation = pq.poll()

//                    println("vvvvvvvvvvvvv moving vvvvvvvvvvvvvv")
//                    printState(state, width)
            creature.moveTo(state, width, goalLocation.x, goalLocation.y)
//                    printState(state, width)
//                    println("^^^^^^^^^^^^^^ moved ^^^^^^^^^^^^^^")

            potentialMap = createPotentialMap(creature, state, width)
//                    println("Moved.  Looking for enemies!")
//                    printPotentialMap(potentialMap, width)

            attackQueue.clear()
            enemies.forEach { enemy ->
              // is the enemy in attack range?  Need to attack the enemy in range with the fewest remaining hit points
              val index = enemy.x + enemy.y * width
              val dist = potentialMap[index]
              if (dist == 1) {
                attackQueue.add(Target(enemy, dist))
              }
            }
            if (attackQueue.isNotEmpty()) {
//                        println("Attacking; newly in range!")
              if (attack(attackQueue, creatures, creature, state, width)) {
                println("An elf has died!!!")
                gameOver = true
              }
            }
          }
        }
        if (!gameOver) {
          roundNumber++
        }
      }

      anElfDied = (numberOfElves != creatures.filterIsInstance<Elf>().size)

//        println()
//        println()
      println("The battle is over!")
//        printState(state, width)
      printStateWithStats(state, width, creatures)
      creatures.forEach { println(it) }
      println("Finished $roundNumber full rounds")
      val remainingHitpoints = creatures.sumBy { it.hitPoints }
      println("Remaining hitpoints: $remainingHitpoints")
      val outcome = remainingHitpoints * roundNumber
      println("Outcome: $outcome")
      if (anElfDied) {
        println("An elf died (elf attack power was ${Elf.attackPower})")
      }
    }
    println("All elves survived with attack power of ${Elf.attackPower}")
  }

  private fun printStateWithStats(state: CharArray, width: Int, creatures: ArrayList<Creature>) {
    var row = 0
    state.toList().windowed(width, width) { rowCharacters ->
      print(rowCharacters.joinToString(""))
      print("    ")
      val stats = ArrayList<String>()
      rowCharacters.forEachIndexed { index, it ->
        if (it == 'E' || it == 'G') {
          val x = index
          val y = row
          var c = findCreatureByLocation(x, y, creatures)
          stats.add("%c(%d)".format(it, c.hitPoints))
        }
      }
      println(stats.joinToString())
      row++
    }
  }

  private fun findCreatureByLocation(
    x: Int,
    y: Int,
    creatures: ArrayList<Creature>
  ): Creature {
    return creatures.find { it.x == x && it.y == y } ?: throw RuntimeException("Really expected to find creature at $x, $y")
  }

  private fun attack(
    attackQueue: PriorityQueue<Target>,
    creatures: ArrayList<Creature>,
    creature: Creature,
    state: CharArray,
    width: Int
  ): Boolean {
    val attackTarget = attackQueue.poll()
    findCreatureById(creatures, attackTarget.creatureId)?.let {
      //            println("$creature attacks $it")
      if (creature.attack(it)) {
        it.die(state, width, creatures)
        println("Creature $it has died!!!")
        if (it is Elf) {
          return true
        }
      }
    }
    return false
  }

  private fun printState(state: CharArray, width: Int) {
    state.toList().windowed(width, width) { row ->
      println(row.joinToString(""))
    }
  }

  private fun createAdjacentPoint(
    state: CharArray,
    targetPotentialMap: IntArray,
    creature: Creature,
    dx: Int,
    dy: Int,
    width: Int
  ): PotentialPoint? {
    val index = creature.x + dx + (creature.y + dy) * width
    if (state[index] == '.') {
      val dist = targetPotentialMap[index]
      return PotentialPoint(creature.x + dx, creature.y + dy, dist)
    }
    return null
  }

  private fun findCreatureById(creatures: List<Creature>, id: Int): Creature? {
    return creatures.find { it.id == id }
  }

  private fun printPotentialMap(map: IntArray, width: Int) {
    map.toList().windowed(width, width) { row ->
      row.forEach {
        when {
          it == 0 -> print("  * ")
          it < Int.MAX_VALUE -> print(" %2d ".format(it))
          else -> print("  . ")
        }
      }
      println()
    }
  }

  data class PotentialPoint(val x: Int, val y: Int, val dist: Int)

  private fun createPotentialMap(creature: Creature, state: CharArray, width: Int): IntArray {
    val map = IntArray(state.size)
    map.fill(Int.MAX_VALUE)

    val nextPoints =
      PriorityQueue<PotentialPoint>(100, compareBy(PotentialPoint::dist, PotentialPoint::y, PotentialPoint::x))
    nextPoints.add(PotentialPoint(creature.x, creature.y, 0))
    map[creature.x + creature.y * width] = 0

    while (nextPoints.isNotEmpty()) {
      val point = nextPoints.poll()
      maybeAddPoint(point, 0, -1, width, state, map)?.let { nextPoints.add(it) }
      maybeAddPoint(point, -1, 0, width, state, map)?.let { nextPoints.add(it) }
      maybeAddPoint(point, 1, 0, width, state, map)?.let { nextPoints.add(it) }
      maybeAddPoint(point, 0, 1, width, state, map)?.let { nextPoints.add(it) }
    }

    return map
  }

  private fun maybeAddPoint(
    point: PotentialPoint,
    dx: Int,
    dy: Int,
    width: Int,
    state: CharArray,
    map: IntArray
  ): PotentialPoint? {
    val nX = point.x + dx
    val nY = point.y + dy
    val nIndex = nY * width + nX
    if (state[nIndex] != '#') {
      val dist = point.dist + 1
      if (dist < map[nIndex]) {
        map[nIndex] = dist
        if (state[nIndex] == '.') {
          // Only continue looking if the map location is passable.  Otherwise, fall through and stop searching
          // for this point
          return PotentialPoint(nX, nY, dist)
        }
      }
    }
    return null
  }
}
