@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2022.day16

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.Deque
import kotlin.math.min
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

  data class Room(val name: String, val index: Int, val rate: Int, val connections: List<Int>)

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
    println(answer.first)
    println(answer.second.map { it.name }.joinToString("\n"))
  }

  private fun openValvesWithPruningNoPath(
    currentRoom: Int,
    unopenedValves: List<Room>,
    minutesLeft: Int,
  ): Pair<Int, MutableList<Room>> {
    if (minutesLeft <= 0 || unopenedValves.isEmpty()) {
      return 0 to mutableListOf()
    }

    var mostPressure = 0
    var valveIndex = 0
    var bestPath = mutableListOf<Room>()

    while (valveIndex < unopenedValves.size) {
      val candidate = unopenedValves[valveIndex]
      val remaining = unopenedValves.removeIndex(valveIndex)
      // Cost in minutes if we open the candidate next
      val cost = costs[currentRoom][candidate.index] + 1
      val newMinutesRemaining = minutesLeft - cost
      if (newMinutesRemaining >= 0) {
        val pressureForThisCandidate = candidate.rate * newMinutesRemaining
        val (actual, path) = openValvesWithPruningNoPath(candidate.index, remaining, newMinutesRemaining)
        val actualPressure = actual + pressureForThisCandidate

        if (actualPressure > mostPressure) {
          mostPressure = actualPressure
          bestPath = path.also { it.add(candidate) }
        }
      }
      valveIndex += 1
    }

    return mostPressure to bestPath
  }

  val totalMinutes = 26

  fun part2() {
    rooms = input.associateBy { it.index }

    println(rooms.size)

    createAllPaths(input).also { (c, p) ->
      costs = c
      paths = p
    }

    val unopenedValves = input.filter { it.rate > 0 }.sortedByDescending { it.rate }

    println(unopenedValves.size)

    val best = bestPath(
      unopenedValves = unopenedValves,
      minutesLeft = totalMinutes,
      currentRoom = 0,
      elephantRoom = 0,
      myRemainingTravelTime = 0,
      elephantRemainingTravelTime = 0
    )

    println(best.totalGain)

    val showFullReport = false

    if (showFullReport) {
      println(best.myGain)
      println(best.elephantGain)
      println()
      println("My path:")
      println(best.myPath.map { it }.reversed().joinToString("\n"))
      println()
      println("Elephant path:")
      println(best.elephantPath.map { it }.reversed().joinToString("\n"))

      println()
      println()
      // val myPath = ArrayDeque(best.myPath.reversed())
      // val elephantPath = ArrayDeque(best.elephantPath.reversed())

      val myPath = ArrayDeque(best.elephantPath.reversed())
      val elephantPath = ArrayDeque(best.myPath.reversed())

      var minute = 1
      val openedValves = mutableListOf<Room>()

      while (minute <= totalMinutes) {
        println("== Minute $minute ==")
        println("Open valves: ${openedValves.joinToString(", ")}")
        println("Released pressure: ${openedValves.sumOf { it.rate }}")
        if (myPath.isNotEmpty() && minute == myPath.first().openedAtMinute - 1) {
          val valve = myPath.removeFirst()
          println("You open valve ${valve.name}.")
          openedValves.add(rooms.getValue(valve.id))
        }

        if (elephantPath.isNotEmpty() && minute == elephantPath.first().openedAtMinute - 1) {
          val valve = elephantPath.removeFirst()
          println("The elephant opens valve ${valve.name}.")
          openedValves.add(rooms.getValue(valve.id))
        }

        println()
        minute++
      }
    }
  }

  data class ValveOpening(
    val id: Int,
    val name: String,
    val openedAtMinute: Int,
    val valveGain: Int,
  )

  data class Step(
    val myGain: Int,
    val elephantGain: Int,
    val myPath: List<ValveOpening>,
    val elephantPath: List<ValveOpening>,
  ) {
    val totalGain = myGain + elephantGain
  }

  private fun bestPath(
    unopenedValves: List<Room>,
    minutesLeft: Int,
    currentRoom: Int,
    elephantRoom: Int,
    myRemainingTravelTime: Int,
    elephantRemainingTravelTime: Int,
  ): Step {
    if (unopenedValves.isEmpty() || minutesLeft <= 0) {
      return Step(0, 0, emptyList(), emptyList())
    }

    val sortedOptions = unopenedValves.map { targetValve ->
      // What is the gain for opening any given value?
      val costInMinutes = costs[currentRoom][targetValve.index] + 1
      val minutesLeftAfterOpeningValve = minutesLeft - costInMinutes
      val gain = targetValve.rate * minutesLeftAfterOpeningValve

      // Estimate the max gain for the rest of the tree
      val remainingValvesMaxGain = unopenedValves.sumOf { valve ->
        if (valve == targetValve) {
          0
        } else {
          (valve.rate) * (minutesLeftAfterOpeningValve + (costs[currentRoom][valve.index] + 1))
        }
      }

      targetValve to (gain + remainingValvesMaxGain)
    }.sortedByDescending { it.second }

    val iterator = sortedOptions.iterator()
    var bestStep = Step(0, 0, emptyList(), emptyList())

    // Who is moving next?
    if (myRemainingTravelTime == 0) {
      while (iterator.hasNext()) {
        val next = iterator.next()

        if (next.second >= bestStep.totalGain) {
          // So you're telling me there's still a chance...
          val targetValve = next.first
          val costInMinutes = costs[currentRoom][targetValve.index] + 1
          val minutesLeftAfterOpeningValve = minutesLeft - costInMinutes

          if (minutesLeftAfterOpeningValve >= 0) {
            val gain = targetValve.rate * minutesLeftAfterOpeningValve

            val remainingValves = unopenedValves.filterNot { it == targetValve }

            // Who is going to finish first?
            val nextTime = min(costInMinutes, elephantRemainingTravelTime)

            val nextStep = bestPath(
              remainingValves,
              minutesLeft - nextTime,
              targetValve.index,
              elephantRoom,
              costInMinutes - nextTime,
              elephantRemainingTravelTime - nextTime
            )

            val actualGain = gain + nextStep.totalGain

            if (actualGain > bestStep.totalGain) {
              val path = nextStep.myPath + ValveOpening(
                id = targetValve.index,
                name = targetValve.name,
                openedAtMinute = totalMinutes - (minutesLeftAfterOpeningValve - 1),
                valveGain = gain
              )
              bestStep = nextStep.copy(
                myGain = nextStep.myGain + gain,
                myPath = path,
              )
            }
          }
        }
      }
    } else {
      // Move the elephant
      while (iterator.hasNext()) {
        val next = iterator.next()

        if (next.second >= bestStep.totalGain) {
          // So you're telling me there's still a chance...
          val targetValve = next.first
          val costInMinutes = costs[elephantRoom][targetValve.index] + 1
          val minutesLeftAfterOpeningValve = minutesLeft - costInMinutes

          if (minutesLeftAfterOpeningValve >= 0) {
            val gain = targetValve.rate * minutesLeftAfterOpeningValve

            val remainingValves = unopenedValves.filterNot { it == targetValve }

            // Who is going to finish first?
            val nextTime = min(myRemainingTravelTime, costInMinutes)

            val nextStep = bestPath(
              remainingValves,
              minutesLeft - nextTime,
              currentRoom,
              targetValve.index,
              myRemainingTravelTime - nextTime,
              costInMinutes - nextTime,
            )

            val actualGain = gain + nextStep.totalGain

            if (actualGain > bestStep.totalGain) {
              val path = nextStep.elephantPath + ValveOpening(
                id = targetValve.index,
                name = targetValve.name,
                openedAtMinute = totalMinutes - (minutesLeftAfterOpeningValve - 1),
                valveGain = gain
              )

              bestStep = nextStep.copy(
                elephantGain = nextStep.elephantGain + gain,
                elephantPath = path,
              )
            }
          }
        }
      }
    }

    return bestStep
  }

  private fun bestPathWorks(unopenedValves: List<Room>, minutesLeft: Int, currentRoom: Int): Pair<Int, MutableList<Room>> {
    if (unopenedValves.isEmpty() || minutesLeft <= 0) {
      return 0 to mutableListOf()
    }

    // Upper limit on the gain -- it is not possible to do better than this value!
    var upperLimit = unopenedValves.sumOf { it.rate } * minutesLeft
    // println(upperLimit)

    val sortedOptions = unopenedValves.map { targetValve ->
      // What is the gain for opening any given value?
      val costInMinutes = costs[currentRoom][targetValve.index] + 1
      val minutesLeftAfterOpeningValve = minutesLeft - costInMinutes
      val gain = targetValve.rate * minutesLeftAfterOpeningValve

      // Estimate the max gain for the rest of the tree
      val remainingValvesMaxGain = unopenedValves.sumOf { valve ->
        if (valve == targetValve) {
          0
        } else {
          (valve.rate) * (minutesLeftAfterOpeningValve + (costs[currentRoom][valve.index] + 1))
        }
      }

      targetValve to (gain + remainingValvesMaxGain)
    }.sortedByDescending { it.second }

    // println(sortedOptions.joinToString("\n"))
    // return 0 to mutableListOf()

    val iterator = sortedOptions.iterator()

    var bestSoFar = -1
    var bestSoFarPath: MutableList<Room>? = null

    while (iterator.hasNext()) {
      val next = iterator.next()

      if (next.second >= bestSoFar) {
        // So you're telling me there's still a chance...
        val targetValve = next.first
        val costInMinutes = costs[currentRoom][targetValve.index] + 1
        val minutesLeftAfterOpeningValve = minutesLeft - costInMinutes

        if (minutesLeftAfterOpeningValve >= 0) {
          val gain = targetValve.rate * minutesLeftAfterOpeningValve

          val remainingValves = unopenedValves.filterNot { it == targetValve }

          val (remainingGain, path) = bestPathWorks(remainingValves,
                                                    minutesLeftAfterOpeningValve,
                                                    targetValve.index)

          val actualGain = gain + remainingGain

          if (actualGain > bestSoFar) {
            bestSoFar = actualGain
            path.add(targetValve)
            bestSoFarPath = path
          }
        }
      }
    }

    if (bestSoFar < 0) {
      // No path found?
      return 0 to mutableListOf()
    }

    return bestSoFar to bestSoFarPath!!
  }

  private fun openValvesWithPruningNoPathPair(
    unopenedValves: List<Room>,
    minutesLeft: Int,
    myRoom: Int,
    myDestination: Int,
    partnerRoom: Int,
    partnerDestination: Int,
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
      val cost = costs[myRoom][candidate.index] + 1
      val newMinutesRemaining = minutesLeft - cost
      if (newMinutesRemaining >= 0) {
        val pressureForThisCandidate = candidate.rate * newMinutesRemaining
        val actual = openValvesWithPruningNoPathPair(remaining,
                                                     newMinutesRemaining,
                                                     candidate.index,
                                                     -1,
                                                     0,
                                                     -1)
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
