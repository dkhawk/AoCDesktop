@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2022.day16

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputFactory
import utils.InputNew
import utils.Remaining
import utils.Template
import utils.Vector

const val day = 16
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<Room>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
    Valve BB has flow rate=13; tunnels lead to valves CC, AA
    Valve CC has flow rate=2; tunnels lead to valves DD, BB
    Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
    Valve EE has flow rate=3; tunnels lead to valves FF, DD
    Valve FF has flow rate=0; tunnels lead to valves EE, GG
    Valve GG has flow rate=0; tunnels lead to valves FF, HH
    Valve HH has flow rate=22; tunnel leads to valve GG
    Valve II has flow rate=0; tunnels lead to valves AA, JJ
    Valve JJ has flow rate=21; tunnel leads to valve II
  """.trimIndent().split("\n")

  @Template("Valve #0 has flow rate=#1; tunnels? leads? to valves? #2")
  data class NamedRoom(val name: String, val rate: Int, @Remaining val connectionString: String) {
    val connections = connectionString.split(", ")

    override fun toString(): String {
      return "Room(valve=$name, rate=$rate, connections=$connections)"
    }
  }

  data class Room(val name: String, val index: Int, val rate: Int, val connections: List<Int>, val turnedOnMinute: Int? = null)

  val nameToIndex = mutableMapOf<String, Int>()

  fun initialize() {
    val lines = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines().filter { it.isNotBlank() }
      realInput
    } else {
      sampleInput
    }

    val inputFactory = InputFactory(NamedRoom::class)
    val namedRooms : List<NamedRoom> = lines.mapNotNull { inputFactory.lineToClass(it) }

    val roomNames = namedRooms.map {
      it.name
    }

    roomNames.sorted().forEach { getRoomIndex(it) }

    input = namedRooms.map { namedRoom ->
      val index = getRoomIndex(namedRoom.name)
      val connections = namedRoom.connections.map { getRoomIndex(it) }
      Room(
        name = namedRoom.name,
        index = index,
        rate = namedRoom.rate,
        connections = connections
      )
    }
  }

  val size = 1000
  val center = Vector(size / 2, size / 2)

  val roomLocations = mutableStateListOf<LocatedRoom>()

  data class LocatedRoom(val location: Vector, val room: Room)

  fun locateRooms() {
    input.take(1).forEach {
      roomLocations.add(LocatedRoom(center, it))
    }
  }

  fun part1() {
    val rooms = input
    println(input.joinToString("\n"))

    val allPaths = createAllPaths(input)

    val unopenedValves = rooms.map { it.index }
    var currentLocation = rooms[0]
    var minutesLeft = 30
    val best = findBestPath(allPaths, unopenedValves, currentLocation, minutesLeft)

    println(best)

    // val gains = rooms.map { targetRoom ->
    //   val timeLost = allPaths[currentLocation][targetRoom.index] + 1
    //   val pressureGained = targetRoom.rate * (minutesLeft - timeLost)
    //   targetRoom.name to pressureGained
    // }
    //
    // println(gains)


    // var currentLocation = "AA"
    //
    // findPath(map, currentLocation, unopenedValves.first().name)

    // Greedy search
  }

  var iterations = 0

  val edges = mutableSetOf<String>()

  private fun findBestPath(
    map: List<List<Int>>,
    unopenedValves: List<Int>,
    currentRoom: Room,
    minutesLeft: Int,
  ): Int {
    // if (iterations > 20) return 0
    // iterations += 1
    // println("$minutesLeft  $currentRoom  unopenedValves: $unopenedValves")
    if (minutesLeft == 0) return 0
    val currentLocation = currentRoom.index
    val otherOpenValves = unopenedValves.filter { it != currentLocation }

    if (otherOpenValves.isEmpty()) {
      return 0
    }

    val options = mutableListOf<Pair<Room, Int>>()

    if (currentLocation in unopenedValves) {
      //   Calculate gain for opening the current value (basically this is just another "path")
      val pressureReleased = (minutesLeft - 1) * currentRoom.rate
      if (pressureReleased > 0) {
        options.add(currentRoom to pressureReleased + findBestPath(
          map,
          otherOpenValves,
          currentRoom,
          minutesLeft - 1
        ))
      }
    }

    // There is a cycle here, but time runs down so... 🤷

    val connectedRooms = currentRoom.connections
      .map { other -> input[other] }.filter {
        "${currentRoom.name}${it.name}" !in edges
      }

    options.addAll(connectedRooms.map {
      edges.add("${currentRoom.name}${it.name}")
      it to findBestPath(map, otherOpenValves, it, minutesLeft - 1)
    })

    val best = options.maxByOrNull { it.second }

    return best?.second ?: 0
  }

  private fun getRoomIndex(name: String) =
    nameToIndex.getOrElse(name) {
      nameToIndex.size.also { nameToIndex[name] = it }
    }

  private fun createAllPaths(rooms: List<Room>): List<List<Int>> {
    val numRooms = rooms.size
    val map = (0 until numRooms).map { outer ->
      (0 until numRooms).map { inner ->
        if (inner == outer) 0 else 999
      }.toMutableList()
    }.toMutableList()

    rooms.forEach { room ->
      room.connections.forEach { connection ->
        map[room.index][connection] = 1
        map[connection][room.index] = 1
      }
    }

    // printMap(map)

    repeat(numRooms) { k ->
      repeat(numRooms) { i ->
        repeat(numRooms) { j ->
          if (map[i][j] > map[i][k] + map[k][j]) {
            map[i][j] = map[i][k] + map[k][j]
          }
        }
      }
    }

    // printMap(map)

    return map.map { it.map { it } }
  }

  private fun printMap(map: List<List<Int>>) {
    val header = ('A'..'J').joinToString(" ")
    println("  $header")
    println(map.withIndex().joinToString("\n") { (index, list) ->
      val l = list.joinToString(" ") {
        if (it >= 999) "∞" else it.toString()
      }
      "${'A' + index} $l"
    }
    )
  }

  private fun findPath(map: Map<String, List<String>>, start: String, goal: String) {
    var current = start
    var cost = 0
    val queue = ArrayDeque<String>()

    queue.addLast(current)

    val visited = HashMap<String, Int>()
    visited[current] = 0

    while (current != start && queue.isNotEmpty()) {
      val next = queue.removeFirst()
      cost += 1
      if (next !in visited) {
        visited[next] = cost
      }
    }
  }

  private fun createMap(namedRooms: List<NamedRoom>): Map<String, List<String>> {

    return namedRooms.associate { it.name to it.connections }
  }

  fun part2() {
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
