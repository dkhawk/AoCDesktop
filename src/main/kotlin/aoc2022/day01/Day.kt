package aoc2022.day01

import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.util.TreeSet
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 0
const val year = 2022

class TopItems<T>(
  private val size: Int = 3,
  private val comparator: Comparator<T>
) {
  private val items = TreeSet<T>(comparator)

  fun getItems(): List<T> = items.toList()

  fun add(item: T) {
    items.add(item)
    while (items.size > size) {
      items.remove(items.first())
    }
  }

  fun clear() {
    items.clear()
  }
}

class Day(private val scope: CoroutineScope) {
  private var inputElves: List<Elf> = emptyList()
  private var input: List<List<Int>> = emptyList()

  private val _elfCursor = MutableStateFlow<Int>(-1)
  private val elfCursor: StateFlow<Int> = _elfCursor

  private val _currentElf = MutableStateFlow<Elf?>(null)
  val currentElf: StateFlow<Elf?> = _currentElf

  var running by mutableStateOf(false)
  private var job: Job? = null

  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  private val topElves = TopItems<Elf>(size = 3, compareBy { it.total })
  var elves by mutableStateOf<List<Elf>>(emptyList())

  var topTotal = derivedStateOf {
    elves.sumOf { it.total }
  }

  var useRealData by mutableStateOf(false)

  val sampleInput = """
    1000
    2000
    3000

    4000

    5000
    6000

    7000
    8000
    9000

    10000
  """.trimIndent().split("\n\n").map { it.split("\n").map { it.toInt() } }

  init {
    scope.launch {
      elfCursor.collect { index -> _currentElf.value = inputElves.getOrNull(index) }
    }

    scope.launch {
      currentElf.filterNotNull().collect {
        topElves.add(it)
        elves = topElves.getItems()
      }
    }
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString().split("\n\n").map { it.split("\n").filter { it.isNotBlank() }.map { it.toInt() } }
      realInput
    } else {
      sampleInput
    }

    inputElves = input.mapIndexed { index, snacks -> toElf(index, snacks) }
  }

  fun part1() {
    val most = input.maxOfOrNull { it.sum() }
    println(most)
  }

  fun part2() {
    val top3 = input.map { it.sum() }.sorted().reversed().take(3)
    println(top3.sum())
  }

  fun execute() {
    job?.cancel()
    job = scope.launch {
      running = true

      while (elfCursor.value < inputElves.lastIndex) {
        step()
        delay(delayTime)
      }

      _elfCursor.value = -1
      running = false
    }
  }

  fun step() {
    _elfCursor.value += 1
  }

  fun stop() {
    job?.cancel()
    running = false
  }

  private fun toElf(index: Int, snackList: List<Int>): Elf = Elf(index, snackList)

  fun reset() {
    stop()
    _elfCursor.value = -1
    topElves.clear()
    elves = emptyList()
  }

  fun updateDataSource(useRealData: Boolean) {
    this.useRealData = useRealData
    initialize()
    reset()
  }
}

data class Elf(
  val id: Int,
  val snacks: List<Int>
) {
  val total by lazy {
    snacks.sum()
  }
}