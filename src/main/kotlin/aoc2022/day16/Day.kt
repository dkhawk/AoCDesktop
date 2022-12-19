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

  private lateinit var rooms: Map<Int, Room>
  private lateinit var costs: List<List<Int>>
  private lateinit var paths: List<List<Int>>

  fun part1() {
    rooms = input.associateBy { it.index }

    println(rooms.size)

    createAllPaths(input).also { (c, p) ->
      costs = c
      paths = p
    }

    val unopenedValves = input.filter { it.rate > 0 }.sortedByDescending { it.rate }

    println(unopenedValves.size)

    val answer = openValvesWithPruningNoPath(0, unopenedValves, 30)
    println(answer)
  }

  fun part2() {
    rooms = input.associateBy { it.index }

    println(rooms.size)

    createAllPaths(input).also { (c, p) ->
      costs = c
      paths = p
    }

    val unopenedValves = input.filter { it.rate > 0 }.sortedByDescending { it.rate }

    println(unopenedValves.size)

    val answer = openValvesWithPruningNoPath(0, unopenedValves, 30)
    println(answer)
  }

  private fun openValvesWithPruningNoPath(
    currentRoom: Int,
    unopenedValves: List<Room>,
    minutesLeft: Int,
  ): Int {
    if (minutesLeft <= 0 || unopenedValves.isEmpty()) {
      return 0
    }

    var mostPressure = 0
    var valveIndex = 0

    while (valveIndex < unopenedValves.size) {
      val candidate = unopenedValves[valveIndex]
      val remaining = unopenedValves.removeIndex(valveIndex)
      // Cost in minutes if we open the candidate next
      val cost = costs[currentRoom][candidate.index] + 1
      val newMinutesRemaining = minutesLeft - cost
      if (newMinutesRemaining >= 0) {
        val pressureForThisCandidate = candidate.rate * newMinutesRemaining
        val actual = openValvesWithPruningNoPath(candidate.index, remaining, newMinutesRemaining)
        val actualPressure = actual + pressureForThisCandidate

        if (actualPressure > mostPressure) {
          mostPressure = actualPressure
        }
      }
      valveIndex += 1
    }

    return mostPressure
  }

  private fun pathFrom(paths: List<List<Int>>, start: String, goal: String) =
    pathFrom(paths, getRoomIndex(start), getRoomIndex(goal)).map { indexToName.getValue(it) }

  private fun pathFrom(paths: List<List<Int>>, start: Int, goal: Int): List<Int> {
    // assume fully connected!!
    val path = mutableListOf<Int>()
    var current = start
    path.add(current)
    while (current != goal) {
      current = paths[current][goal]
      path.add(current)
    }

    return path
  }

  val indexToName = mutableMapOf<Int, String>()

  private fun getRoomIndex(name: String) =
    nameToIndex.getOrElse(name) {
      nameToIndex.size.also {
        indexToName[it] = name
        nameToIndex[name] = it
      }
    }

  private fun createAllPaths(rooms: List<Room>): Pair<List<List<Int>>, List<List<Int>>> {
    val numRooms = rooms.size
    val map = (0 until numRooms).map { outer ->
      (0 until numRooms).map { inner ->
        if (inner == outer) 0 else 999
      }.toMutableList()
    }.toMutableList()

    val paths = (0 until numRooms).map { outer ->
      (0 until numRooms).map { inner ->
        if (inner == outer) inner else 999
      }.toMutableList()
    }.toMutableList()

    rooms.forEach { room ->
      room.connections.forEach { connection ->
        map[room.index][connection] = 1
        map[connection][room.index] = 1
        paths[room.index][connection] = connection
        paths[connection][room.index] = room.index
      }
    }

    repeat(numRooms) { k ->
      repeat(numRooms) { i ->
        repeat(numRooms) { j ->
          if (map[i][j] > map[i][k] + map[k][j]) {
            map[i][j] = map[i][k] + map[k][j]
            paths[i][j] = paths[i][k]
          }
        }
      }
    }

    val pathsFixed = paths as List<List<Int>>
    val costs = map.map { it.map { it } }
    return costs to pathsFixed
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

private fun <E> List<E>.removeIndex(index: Int): List<E> {
  val out = this.subList(0, index).toMutableList()
  if (index <= this.lastIndex) {
    out.addAll(this.subList(index + 1, this.size))
  }
  return out
}
