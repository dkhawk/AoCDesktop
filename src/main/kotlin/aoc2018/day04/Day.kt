@file:OptIn(ExperimentalStdlibApi::class, ExperimentalStdlibApi::class)

package aoc2018.day04

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputFactory
import utils.InputNew
import utils.Template
import utils.packageToYearDay

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    [1518-11-01 00:00] Guard #10 begins shift
    [1518-11-01 00:05] falls asleep
    [1518-11-01 00:25] wakes up
    [1518-11-01 00:30] falls asleep
    [1518-11-01 00:55] wakes up
    [1518-11-01 23:58] Guard #99 begins shift
    [1518-11-02 00:40] falls asleep
    [1518-11-02 00:50] wakes up
    [1518-11-03 00:05] Guard #10 begins shift
    [1518-11-03 00:24] falls asleep
    [1518-11-03 00:29] wakes up
    [1518-11-04 00:02] Guard #99 begins shift
    [1518-11-04 00:36] falls asleep
    [1518-11-04 00:46] wakes up
    [1518-11-05 00:03] Guard #99 begins shift
    [1518-11-05 00:45] falls asleep
    [1518-11-05 00:55] wakes up
  """.trimIndent().split("\n")

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  sealed class Event() {
    abstract val time: MyDateTime
    data class BeginShift(override val time: MyDateTime, val id: Int): Event()
    data class FallAsleep(override val time: MyDateTime): Event()
    data class WakeUp(override val time: MyDateTime): Event()
  }

  fun part1() {
    val events = events()

    val guardSleepTime = mutableMapOf<Int, Long>()
    var activeGuard = -1
    var startOfSleep = MyDateTime(0,0,0,0,0)
    events.forEach { event ->
      when (event) {
        is Event.BeginShift -> activeGuard = event.id
        is Event.FallAsleep -> startOfSleep = event.time
        is Event.WakeUp -> updateSleepTime(guardSleepTime, activeGuard, startOfSleep, event.time)
      }
    }

    println(guardSleepTime)
    val g = guardSleepTime.maxByOrNull { it.value }!!
    println(g)

    val sleepiestGuard = g.key

    val guardSleepFrequency = mutableMapOf<Int, Int>()

    events.forEach { event ->
      when (event) {
        is Event.BeginShift -> activeGuard = event.id
        is Event.FallAsleep -> startOfSleep = event.time
        is Event.WakeUp -> if (activeGuard == sleepiestGuard)
          recordSleepTime(guardSleepFrequency, startOfSleep, event.time)
      }
    }

    val sleepiestMinute = guardSleepFrequency.maxByOrNull { it.value }!!
    println(sleepiestMinute)

    println((sleepiestGuard * sleepiestMinute.key))
  }

  private fun events(): List<Event> {
    val events = input.shuffled().mapNotNull { line ->
      when {
        line.contains("wakes up") -> Event.WakeUp(line.getTime())
        line.contains("falls asleep") -> Event.FallAsleep(line.getTime())
        line.contains("begins shift") -> Event.BeginShift(line.getTime(), line.getGuardId())
        else -> null
      }
    }.sortedBy { it.time }
    return events
  }

  private fun recordSleepTime(
    guardSleepFrequency: MutableMap<Int, Int>,
    startOfSleep: MyDateTime,
    endOfSleep: MyDateTime,
  ) {
    var start = startOfSleep.toLocalDateTime()
    val end = endOfSleep.toLocalDateTime()

    while (start < end) {
      guardSleepFrequency[start.minute] = guardSleepFrequency.getOrDefault(start.minute, 0) + 1
      start = start.plusMinutes(1)
    }
  }

  private fun updateSleepTime(
    guardSleepTime: MutableMap<Int, Long>,
    activeGuard: Int,
    startOfSleep: MyDateTime,
    endOfSleep: MyDateTime
  ) {
    val sleepMinutes = endOfSleep - startOfSleep
    guardSleepTime.add(activeGuard, sleepMinutes)
  }

  fun part2() {
    val events = events()

    val schedule = mutableMapOf<Int, MutableMap<Int, Int>>()
    var activeGuard = -1
    var startOfSleep = MyDateTime(0,0,0,0,0)
    events.forEach { event ->
      when (event) {
        is Event.BeginShift -> activeGuard = event.id
        is Event.FallAsleep -> startOfSleep = event.time
        is Event.WakeUp -> updateSleepSchedule(schedule, activeGuard, startOfSleep, event.time)
      }
    }

    val answer = schedule.maxByOrNull { (id, sch) ->
      sch.maxByOrNull { (min, count) -> count }?.value ?: 0
    }!!

    val minute = answer.value.maxByOrNull { it.value }!!

    println(answer)
    println(answer.key * minute.key)
  }

  private fun updateSleepSchedule(
    schedule: MutableMap<Int, MutableMap<Int, Int>>,
    activeGuard: Int,
    startOfSleep: MyDateTime,
    endOfSleep: MyDateTime,
  ) {
    val gs = schedule.getOrPut(activeGuard) { mutableMapOf() }
    recordSleepTime(gs, startOfSleep, endOfSleep)
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

private fun MutableMap<Int, Long>.add(activeGuard: Int, sleepMinutes: Long) {
  this[activeGuard] = getOrDefault(activeGuard, 0) + sleepMinutes
}

private fun String.getGuardId(): Int {
  return substringAfter('#').substringBefore(' ').toInt()
}

@Template("\\[#0-#1-#2 #3:#4\\].*")
data class MyDateTime(val year: Int, val month: Int, val day: Int, val hour: Int, val minute: Int) :
  Comparable<MyDateTime> {
  override fun compareTo(other: MyDateTime): Int {
    val ps = toPaddedString()
    val ops = other.toPaddedString()
    return ps.compareTo(ops)
  }

  private fun toPaddedString(): String {
    return "%04d%02d%02d%02d%02d".format(year, month, day, hour, minute)
  }

  operator fun minus(other: MyDateTime): Long {
    return ChronoUnit.MINUTES.between(other.toLocalDateTime(), toLocalDateTime())
  }

  fun toLocalDateTime(): LocalDateTime {
    return LocalDateTime.of(year, month, day, hour, minute)
  }
}

val myDateFactory = InputFactory(MyDateTime::class)

private fun String.getTime(): MyDateTime = myDateFactory.lineToClass<MyDateTime>(this)!!