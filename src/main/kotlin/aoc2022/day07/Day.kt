package aoc2022.day07

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.function.BiPredicate
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 7
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    ${'$'} cd /
    ${'$'} ls
    dir a
    14848514 b.txt
    8504156 c.dat
    dir d
    ${'$'} cd a
    ${'$'} ls
    dir e
    29116 f
    2557 g
    62596 h.lst
    ${'$'} cd e
    ${'$'} ls
    584 i
    ${'$'} cd ..
    ${'$'} cd ..
    ${'$'} cd d
    ${'$'} ls
    4060174 j
    8033020 d.log
    5626152 d.ext
    7214296 k""".trimIndent().split("\n").filter { it.isNotBlank() }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  fun part1() {
    val root = processInput(input)

    // Find all of the directories with a total size of at most 100000. What is the sum of the total sizes of those directories?
    val entries = findEntries(root) {
      it is Entry.Directory && it.size <= 100_000
    }

    // println(entries.joinToString("\n") { "${it.name}: ${it.size}" })
    println("Sum of entries smaller than 100,000: ${entries.sumOf { it.size }}")
  }

  fun part2() {
    val diskSize = 70000000
    val needed = 30000000

    val root = processInput(input)

    val used = root.size
    val available = diskSize - used

    val needToDelete = needed - available

    val entries = findEntries(root) {
      it is Entry.Directory && it.size >= needToDelete
    }

    val target = entries.minByOrNull { it.size }!!
    println("Smallest directory to delete to free up $needToDelete space: " +
              "${target.name}, ${target.size}")
  }

  sealed class Entry() {
    abstract val size: Int
    abstract val parent: Entry?
    abstract val name: String

    data class Directory(
      override val name: String,
      override var size: Int,
      val children: MutableList<Entry>,
      override val parent: Entry?,
    ): Entry()

    data class File(
      override val name: String,
      override val size: Int,
      override val parent: Entry,
    ): Entry()
  }

  private val numRegEx = Regex("""^\d+.*""".trimMargin())

  private fun processInput(input: List<String>): Entry.Directory {
    val root = Entry.Directory("/", 0, mutableListOf(), null)
    var current = root

    input.drop(1).forEach { line ->
      when {
        line.startsWith("\$ cd ..") -> {
          if (current.parent != null) {
            current = current.parent as Entry.Directory
          }
        }
        line.startsWith("\$ cd") -> {
          val name = line.substring(5)
          val dir = Entry.Directory(name, 0, mutableListOf(), current)
          current.children.add(dir)
          current = dir
        }
        line.matches(numRegEx) -> {
          val (sizeStr, name) = line.split(" ")
          val size = sizeStr.toInt()
          val file = Entry.File(name, size, current)
          current.children.add(file)
        }
        else -> {}
      }
    }

    calculateSizes(root)
    return root
  }

  private fun findEntries(path: Entry.Directory, predicate: (Entry) -> Boolean): List<Entry> {
    val result = mutableListOf<Entry>()
    if ( predicate(path) ) {
      result.add(path)
    }

    path.children.forEach {
      when (it) {
        is Entry.File -> if ( predicate(it) ) { result.add(it) }
        is Entry.Directory -> result.addAll(findEntries(it, predicate))
      }
    }

    return result
  }

  private fun calculateSizes(path: Entry.Directory) : Int {
    val size = path.children.sumOf {
      when (it) {
        is Entry.File -> it.size
        is Entry.Directory -> calculateSizes(it)
      }
    }

    path.size = size
    return size
  }

  private fun printTree(path: Entry.Directory, depth: Int = 0) {
    val prefix = (0 until depth).map { "  " }.joinToString("")
    println("$prefix- ${path.name} (dir, size=${path.size})")

    path.children.sortedBy { it.name }.forEach {
      when (it) {
        is Entry.File -> println("$prefix  - ${it.name} (file, size=${it.size})")
        is Entry.Directory -> printTree(it, depth + 1)
      }
    }
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
