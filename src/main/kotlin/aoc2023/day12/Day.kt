package aoc2023.day12

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.common.truth.Truth.assertThat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import utils.COLORS
import utils.InputNew
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    ???.### 1,1,3
    .??..??...?##. 1,1,3
    ?#?#?#?#?#?#?#? 1,3,1,6
    ????.#...#... 4,1,1
    ????.######..#####. 1,6,5
    ?###???????? 3,2,1
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

  fun part1() {
    // val validExamples = """
    //   #.#.### 1,1,3
    //   .#...#....###. 1,1,3
    //   .#.###.#.###### 1,3,1,6
    //   ####.#...#... 4,1,1
    //   #....######..#####. 1,6,5
    //   .###.##....# 3,2,1
    // """.trimIndent().split('\n')
    //
    // val examples = validExamples.map { it.split(' ') }
    //   .map { it[0] to it[1].split(',').map { it.toInt() } }
    //
    // examples.forEach { (s, groups) ->
    //   val valid = s.isValid(groups)
    //   println("$s $groups is $valid")
    // }
    //
    // return

    // naive solution

    // "#?#".toCombos(1)
    // "???".toCombos(1)


    assertThat("#.#.###".toCombosWithDots(listOf(1, 1, 3).sortedDescending())).isEqualTo(1)


    // assertThat("##".toCombos(1)).isEqualTo(0)
    // assertThat("##".toCombos(2)).isEqualTo(1)
    // assertThat("??".toCombos(2)).isEqualTo(1)
    //
    // assertThat("?#?".toCombos(2)).isEqualTo(2)

    return

    val size = 1
    val re = Regex("[?#]{$size}")
    println(re.pattern)

    listOf("#", "?", "??").forEach { s ->
      println("-> $s")
      re.findAll(s).forEach { result ->
        println(result.groups[0])
      }
    }

    return

    testCombination(listOf("?"), listOf(1), 1)
    testCombination(listOf("??"), listOf(1), 2)
    testCombination(listOf("#?"), listOf(1), 1)
    testCombination(listOf("?#"), listOf(1), 1)

    return

    println(
      countValidCombinations(
        listOf("???"),
        listOf(1, 1)
      )
    )

    return

    val records = input.map { line -> line.toRecord() }

    val answer = runBlocking {
      withContext(Dispatchers.Default) {
        val jobs = records.take(1).map { (record, damagedSpringGroups) ->
          async {
            validCombos(record, damagedSpringGroups)
            // record.count { it == '?' }.also { println(it) }
          }
        }
        jobs.awaitAll().sum()
      }
    }

    println(answer)
  }

  private fun testToCombos() {
    assertThat("?????".toCombos(listOf(1))).isEqualTo(5)
    assertThat("?????".toCombos(listOf(2))).isEqualTo(4)
    assertThat("?????".toCombos(listOf(3))).isEqualTo(3)

    assertThat("?".toCombos(listOf(1))).isEqualTo(1)
    assertThat("#".toCombos(listOf(1))).isEqualTo(1)

    assertThat("??".toCombos(listOf(1))).isEqualTo(2)
    assertThat("?#".toCombos(listOf(1))).isEqualTo(1)
    assertThat("#?".toCombos(listOf(1))).isEqualTo(1)
    assertThat("##".toCombos(listOf(1))).isEqualTo(0)

    assertThat("???".toCombos(listOf(1))).isEqualTo(3)
    assertThat("#??".toCombos(listOf(1))).isEqualTo(2)
    assertThat("?#?".toCombos(listOf(1))).isEqualTo(1)
    assertThat("??#".toCombos(listOf(1))).isEqualTo(2)

    assertThat("##?".toCombos(listOf(1))).isEqualTo(0)
    assertThat("?##".toCombos(listOf(1))).isEqualTo(0)
    assertThat("#?#".toCombos(listOf(1))).isEqualTo(2)

    assertThat("###".toCombos(listOf(1))).isEqualTo(0)

    assertThat("?".toCombos(listOf(2))).isEqualTo(0)
    assertThat("#".toCombos(listOf(2))).isEqualTo(0)

    assertThat("??".toCombos(listOf(2))).isEqualTo(1)
    assertThat("?#".toCombos(listOf(2))).isEqualTo(1)
    assertThat("#?".toCombos(listOf(2))).isEqualTo(1)
    assertThat("##".toCombos(listOf(2))).isEqualTo(1)

    assertThat("???".toCombos(listOf(2))).isEqualTo(2)
    assertThat("#??".toCombos(listOf(2))).isEqualTo(1)
    assertThat("?#?".toCombos(listOf(2))).isEqualTo(2)
    assertThat("??#".toCombos(listOf(2))).isEqualTo(1)

    assertThat("##?".toCombos(listOf(2))).isEqualTo(1)
    assertThat("?##".toCombos(listOf(2))).isEqualTo(1)
    assertThat("#?#".toCombos(listOf(2))).isEqualTo(0)

    assertThat("###".toCombos(listOf(2))).isEqualTo(0)
  }

  private fun testCombination(groups: List<String>, sizes: List<Int>, expected: Int) {
    val answer = countValidCombinations(groups, sizes)
    if (answer != expected) {
      println("Expected $expected. Actual $answer.  $groups  $sizes")
    }
  }

  private fun validCombos(record: String, damagedSpringGroups: List<Int>): Int {
    // Battleship approach
    val unfulfilledGroupsSorted = damagedSpringGroups.sortedDescending().toMutableList()
    println(unfulfilledGroupsSorted)

    val recordGroups = record.split(Regex("""[.]+""")).filterNot { it.isEmpty() }
    println(recordGroups)

    val (wildGroups, exactGroups) = recordGroups.partition { it.contains('?') }
    println(wildGroups)
    println(exactGroups)

    exactGroups.map { it.length }.forEach { len ->
      if (!unfulfilledGroupsSorted.remove(len)) {
        throw Exception("No unfulfilled group with length $len $unfulfilledGroupsSorted")
      }
    }

    println(unfulfilledGroupsSorted)

    countValidCombinations(wildGroups.sortedByDescending { it.length }, unfulfilledGroupsSorted)

    return 1
  }

  private fun countValidCombinations(
    wildGroupsSortedDes: List<String>,
    unfulfilledGroupsSortedDes: List<Int>,
  ): Int {
    if (wildGroupsSortedDes.size == 1 && unfulfilledGroupsSortedDes.size == 1) {
      val group = wildGroupsSortedDes.first()
      val size = unfulfilledGroupsSortedDes.first()
      if (group.length == size)
        return 1

      // Replace all '?' with either '.' or '#'
      // buildString {
      //   append("[?#]")
      //   append('{')
      //   append(size)
      //   append('}')
      // }
      Regex("[?#]{$size}")
      group
    }

    // Fit the largest unfulfilled group
    val nextLargest = unfulfilledGroupsSortedDes.first()
    val remainingGroups = unfulfilledGroupsSortedDes.drop(1)
    // Where will the nextLargest fit

    val (fit, unfit) = wildGroupsSortedDes.partition { it.length >= nextLargest }

    fit.forEach {s ->

    }


    TODO("Not yet implemented")
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

private fun String.toCombosWithDots(sizes: List<Int>): Int {
  return split(Regex("\\.+")).sumOf { it.toCombos(sizes) }
}

private fun String.toCombos(sizes: List<Int>): Int {
  if (sizes.isEmpty()) return 0

  val size = sizes.first()

  if (this.length < size) return 0

  val matches = (0 until (length - (size - 1))).map { start ->
    val end = start + size
    val prev = if (start > 0) get(start - 1) else null
    val next = if (end < length) get(end) else null
    when {
      // The bookends must not be '#' (or the string boundary)
      prev == '#' -> null
      next == '#' -> null
      else -> {
        val substring = this.substring(start, end)
        val before = if (start > 1) this.substring(0, start - 1) else null
        val after = if (end < lastIndex) this.substring(end + 1) else null
        Triple(before, substring, after)
      }
    }
  }

  val total = matches.count { it != null }
  println("$this $size => $total")
  println(matches.joinToString("\n") { "${it?.first} ${COLORS.RED}${it?.second}${COLORS.NONE} ${it?.third}" })
  println()

  val others = matches.sumOf {
    (it?.first?.toCombos(sizes.drop(1)) ?: 0) +
    (it?.third?.toCombos(sizes.drop(1)) ?: 0)
  }

  return total + others
}

private fun String.toRecord(): Pair<String, List<Int>> {
  return split(' ').let { parts ->
    parts[0] to parts[1].split(',').map { it.toInt() } }
}

private fun String.isValid(groups: List<Int>): Boolean {
  // Don't know why I can't make this greedy!
  val split = this.split(Regex("""[.]+""")).filterNot { it.isEmpty() }
  val brokenGroupCounts = split.map { it.length }.groupingBy { it }.eachCount().toSortedMap()
  val checkSumGroupCounts = groups.groupingBy { it }.eachCount().toSortedMap()
  return brokenGroupCounts == checkSumGroupCounts
}
