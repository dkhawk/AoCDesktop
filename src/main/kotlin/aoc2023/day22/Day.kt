package aoc2023.day22

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.Direction3d
import utils.InputNew
import utils.Vector
import utils.Vector3d
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    1,0,1~1,2,1
    0,0,2~2,0,2
    0,2,3~2,2,3
    0,0,4~0,2,4
    2,0,5~2,2,5
    0,1,6~2,1,6
    1,1,8~1,1,9
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

  data class ColumnCube(val zIndex: Int, val brickIndex: Int)

  fun part1() {
    val bricks = input.mapIndexed { index, line -> line.toBrick(index) }

    val settledBricks = mutableListOf<Brick>()
    val unsettledBricks = mutableListOf<Brick>()

    unsettledBricks.addAll(bricks)

    val settledCubes = settledBricks.flatMap { brick -> brick.cubes }.toMutableSet()

    val unsettledCubes = unsettledBricks.flatMap { brick -> brick.cubes }.toMutableSet()

    while (unsettledBricks.isNotEmpty()) {
      val brick = unsettledBricks.removeAt(0)

      if (brick.isSettled(settledCubes)) {
        settledBricks.add(brick)
        settledCubes.addAll(brick.cubes)
        unsettledCubes.removeAll(brick.cubes.toSet())
      } else {
        unsettledCubes.removeAll(brick.cubes.toSet())

        var newBrick = brick

        while (!newBrick.isSettled(settledCubes) && !newBrick.isSettled(unsettledCubes)) {
          newBrick = newBrick.advance(Direction3d.DOWN_Z)
        }

        if (newBrick.cubes.any { it in unsettledCubes }) throw Exception("cube is already in unsettled cubes")
        if (newBrick.cubes.any { it in settledCubes }) throw Exception("cube is already in settled cubes")

        unsettledCubes.addAll(newBrick.cubes)
        unsettledBricks.add(newBrick)
      }
    }

    // println(settledBricks)
    // println(settledCubes)

    // Now find all bricks only supported by another brick
    val cubesWithIds = settledBricks.flatMap { brick -> brick.cubes.map { cube -> cube to brick.id } }.toMap()
    // println(cubesWithIds)

    val supportingBricks = settledBricks.map { brick ->
      brick.id to brick.supportingCubes(cubesWithIds)
    }

    // println(supportingBricks)

    val bricksSupportedByASingleOtherBrick = supportingBricks.filter { it.second.size == 1 }.map { it.first to it.second.first() }

    // println(bricksSupportedByASingleOtherBrick.joinToString("\n") { (self, other) ->
    //   "${'A' + self} is supported only by ${'A' + other}"
    // })

    val doNotDisintegrate = bricksSupportedByASingleOtherBrick.map { it.second }.toSet()

    val safeToDisintegrate = bricks.map { it.id }.filter { it !in doNotDisintegrate }

    // println(safeToDisintegrate.map { 'A' + it })

    println(safeToDisintegrate.size)

    // val supportedBy = bricks.map { brick ->
    //   val id = brick.id
    //   val bricksSupportedBy = supportingBricks.filter { id in it.second }.map { it.first }
    //   id to bricksSupportedBy
    // }
    //
    // println(supportedBy)


    // println(supportingBricks.map { 'A' + it })
    //
    // val unsupportingBricks = bricks.filter { it.id !in supportingBricks }
    //
    // println(unsupportingBricks.map { 'A' + it.id })
    //
    // println(unsupportingBricks.size)
  }

  fun part2() {
    val bricks = input.mapIndexed { index, line -> line.toBrick(index) }

    val settledBricks = mutableListOf<Brick>()
    val unsettledBricks = mutableListOf<Brick>()

    unsettledBricks.addAll(bricks)

    val settledCubes = settledBricks.flatMap { brick -> brick.cubes }.toMutableSet()

    val unsettledCubes = unsettledBricks.flatMap { brick -> brick.cubes }.toMutableSet()

    while (unsettledBricks.isNotEmpty()) {
      val brick = unsettledBricks.removeAt(0)

      if (brick.isSettled(settledCubes)) {
        settledBricks.add(brick)
        settledCubes.addAll(brick.cubes)
        unsettledCubes.removeAll(brick.cubes.toSet())
      } else {
        unsettledCubes.removeAll(brick.cubes.toSet())

        var newBrick = brick

        while (!newBrick.isSettled(settledCubes) && !newBrick.isSettled(unsettledCubes)) {
          newBrick = newBrick.advance(Direction3d.DOWN_Z)
        }

        if (newBrick.cubes.any { it in unsettledCubes }) throw Exception("cube is already in unsettled cubes")
        if (newBrick.cubes.any { it in settledCubes }) throw Exception("cube is already in settled cubes")

        unsettledCubes.addAll(newBrick.cubes)
        unsettledBricks.add(newBrick)
      }
    }

    // println(settledBricks)
    // println(settledCubes)

    // Now find all bricks only supported by another brick
    val cubesWithIds = settledBricks.flatMap { brick -> brick.cubes.map { cube -> cube to brick.id } }.toMap()
    // println(cubesWithIds)

    val supportingBricks = settledBricks.map { brick ->
      brick.id to brick.supportingCubes(cubesWithIds)
    }

    println(supportingBricks)

    val bricksSupportedByASingleOtherBrick = supportingBricks.filter { it.second.size == 1 }.map { it.first to it.second.first() }

    println(bricksSupportedByASingleOtherBrick.joinToString("\n") { (self, other) ->
      "${'A' + self} is supported only by ${'A' + other}"
    })

    // val doNotDisintegrate = bricksSupportedByASingleOtherBrick.map { it.second }.toSet()
    //
    // val safeToDisintegrate = bricks.map { it.id }.filter { it !in doNotDisintegrate }
    //
    // // println(safeToDisintegrate.map { 'A' + it })
    //
    // println(safeToDisintegrate.size)
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

data class Brick(val id: Int, val cubes: List<Vector3d>) {
  fun toColumns(): List<Pair<Vector, Pair<Int, Int>>> {
    return cubes.map { vector3d ->
      val key = Vector(vector3d.x, vector3d.y)
      val value = vector3d.z to id
      key to value
    }
  }

  fun isSettled(settledCubes: Set<Vector3d>): Boolean {
    return cubes.any { it.z == 1 } || cubes.any { it.advance(Direction3d.DOWN_Z) in settledCubes }
  }

  override fun toString(): String {
    return "$id: ${cubes}"
  }

  fun advance(direction: Direction3d): Brick = Brick(id, cubes.map { it.advance(direction) })

  fun supportingCubes(cubesWithIds: Map<Vector3d, Int>): Set<Int> {
    return cubes.mapNotNull { cube ->
      cubesWithIds[cube.advance(Direction3d.DOWN_Z)]
    }.filter { it != id }.toSet()
  }
}

private fun String.toBrick(index: Int): Brick {
  return split('~')
    .map { it.split(',').map { it.toInt() } }
    .map { it.toVector3d() }
    .toBrick(index)
}

private fun hydrate(start: Vector3d, end: Vector3d): List<Vector3d> {
  if (start == end) {
    return listOf(start)
  }

  val heading = start.headingTo(end)
  var current = start
  return buildList {
    add(current)
    while (current != end) {
      current = current.advance(heading)
      add(current)
    }
  }
}

private fun List<Vector3d>.toBrick(index: Int) = Brick(id = index, hydrate(this[0], this[1]))

private fun List<Int>.toVector3d() = Vector3d(this[0], this[1], this[2],)
