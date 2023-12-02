package aoc2023.day02

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  var part: Int = 0
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
    Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
    Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
    Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
    Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green
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

  data class Round(val red: Int, val green: Int, val blue: Int)

  enum class Colors {
    RED,
    GREEN,
    BLUE
  }

  fun part1() {
    // parseGame2(input.first())
    //
    // return
    val maxRound = Round(12, 13, 14)

    val games = input.associate { line ->
      parseGame(line)
    }

    val maxes = games.map { (id, rounds) ->
      val m = rounds.unzip().map { it.maxOf { it } }
      id to Round(red = m[0], green = m[1], blue = m[2])
    }

    val validGames = maxes.filter { (id, maxCubes) ->
      maxCubes.red <= maxRound.red &&
        maxCubes.green <= maxRound.green &&
        maxCubes.blue <= maxRound.blue
    }

    println(validGames.sumOf { it.first })
  }

  val gameRegex = Regex("""Game (?<id>\d+): (?<rounds>(((\d+) (\w+))(, ((\d+) (\w+)))*)(; ((\d+) (\w+))(, ((\d+) (\w+)))*)*)""")

  private fun parseGame2(line: String): Pair<Int, List<Round>> {
    val match = gameRegex.find(line)
    if (match != null) {
      println(match.groups["id"]?.value)
      println(match.groups["rounds"]?.value)
      println(match.groups.joinToString("\n"))
    }

    return 42 to listOf()
  }

  private fun parseGame(line: String): Pair<Int, List<Round>> {
    val (gameString, roundStrings) = line.split(":")
    val (_, idString) = gameString.split(' ')
    val id = idString.toInt()

    val rounds = roundStrings.split(';').map { rs ->
      val cubes = rs.split(',').map { colorString ->
        val (number, colorString) = colorString.trim().split(' ')
        val color = when {
          colorString.contains("red") -> Colors.RED
          colorString.contains("green") -> Colors.GREEN
          colorString.contains("blue") -> Colors.BLUE
          else -> throw Exception("Invalid color [$rs] [$colorString]")
        }
        color to number.trim().toInt()
      }
      val red = cubes.firstOrNull { it.first == Colors.RED }?.second ?: 0
      val green = cubes.firstOrNull { it.first == Colors.GREEN }?.second ?: 0
      val blue = cubes.firstOrNull { it.first == Colors.BLUE }?.second ?: 0
      Round(red, green, blue)
    }

    return id to rounds
  }

  fun part2() {
    val games = input.associate { line ->
      parseGame(line)
    }

    val maxes = games.map { (id, rounds) ->
      rounds.unzip().map { it.maxOf { it } }
    }

    val power = maxes.sumOf { m ->
      m.fold(1.toLong()) { a, b ->
        a * b
      }
    }

    println(power)
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

private fun List<Day.Round>.unzip(): List<List<Int>> {
  val listRed = ArrayList<Int>(100)
  val listGreen = ArrayList<Int>(100)
  val listBlue = ArrayList<Int>(100)

  for (round in this) {
    listRed.add(round.red)
    listGreen.add(round.green)
    listBlue.add(round.blue)
  }

  return listOf(listRed, listGreen, listBlue)
}
