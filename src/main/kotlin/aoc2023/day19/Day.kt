package aoc2023.day19

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
  private lateinit var input: List<String>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    px{a<2006:qkq,m>2090:A,rfg}
    pv{a>1716:R,A}
    lnx{m>1548:A,A}
    rfg{s<537:gd,x>2440:R,A}
    qs{s>3448:A,lnx}
    qkq{x<1416:A,crn}
    crn{x>2662:A,R}
    in{s<1351:px,qqz}
    qqz{s>2770:qs,m<1801:hdj,R}
    gd{a>3333:R,R}
    hdj{m>838:A,pv}

    {x=787,m=2655,a=1222,s=2876}
    {x=1679,m=44,a=2067,s=496}
    {x=2036,m=264,a=79,s=2244}
    {x=2461,m=1339,a=466,s=291}
    {x=2127,m=1623,a=2188,s=1013}
  """.trimIndent().split('\n')

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput
    }
  }

  class Part(categoriesIn: List<Pair<Char, Int>>) {
    val categories = categoriesIn.toMap()

    override fun toString(): String {
      return categories.toString()
    }

    fun toRating() = categories.values.sum()
  }

  sealed class Rule() {
    abstract val destination: Destination
    abstract operator fun invoke(part: Part): Destination

    data class LessThan(val category: Char, val operand: Int, override val destination: Destination) : Rule() {
      override operator fun invoke(part: Part): Destination {
        return if (part.categories.getValue(category) < operand) destination else Destination.Nope
      }
    }

    data class GreaterThan(val category: Char, val operand: Int, override val destination: Destination) : Rule() {
      override operator fun invoke(part: Part): Destination {
        return if (part.categories.getValue(category) > operand) destination else Destination.Nope
      }
    }

    data class Unconditional(override val destination: Destination) : Rule() {
      override operator fun invoke(part: Part): Destination {
        return destination
      }
    }
  }

  sealed class Destination {
    object Accepted : Destination() {
      override fun toString() = "Accepted"
    }

    object Rejected: Destination() {
      override fun toString() = "Rejected"
    }

    data class NewRule(val name: String): Destination() {
      override fun toString() = name
    }

    object Nope: Destination() {
      override fun toString() = "Nope!"
    }
  }

  class Workflow(val rules: List<Rule>) {
    operator fun invoke(part: Part) : Destination {
      return rules.first { it(part) != Destination.Nope }.destination
    }

    override fun toString(): String {
      return rules.toString()
    }
  }

  fun part1() {
    val (partsStrings, workflowStrings) = input
      .filter(String::isNotBlank)
      .partition { it.startsWith('{') }

    val parts = partsStrings.map { it.toPart() }
    val workflows = workflowStrings.associate { it.toWorkflow() }

    val results = parts.map { part ->
      var destination: Destination = Destination.NewRule("in")
      while (destination !is Destination.Accepted && destination !is Destination.Rejected) {
        val key = (destination as Destination.NewRule).name
        val workflow = workflows.getValue(key)
        destination = workflow(part)
      }
      part to destination
    }

    val sum = results.filter { it.second is Destination.Accepted }.sumOf {(part, _) ->
      part.toRating()
    }

    println(sum)
  }

  data class RangeMap(val range: IntRange, val destination: Destination)

  data class PartRanges(val destination: Destination, val categoriesToRangeMaps: Map<Char, IntRange>)

  fun part2() {
    val min = 1
    val max = 4000

    val categories = listOf('x', 'm', 'a', 's')

    val (_, workflowStrings) = input
      .filter(String::isNotBlank)
      .partition { it.startsWith('{') }

    val workflows = workflowStrings.associate { it.toWorkflow() }
    // val workflows = listOf("in{s<1351:A,R}").associate { it.toWorkflow() }

    val initialState = PartRanges(Destination.NewRule("in"), categories.associateWith { min..max })

    val unresolvedParts = ArrayDeque<PartRanges>()

    unresolvedParts.addLast(initialState)

    val accepted = mutableListOf<PartRanges>()

    while (unresolvedParts.isNotEmpty()) {
      var partRanges = unresolvedParts.removeFirst()

      if (partRanges.destination == Destination.Accepted) {
        accepted.add(partRanges)
        continue
      } else if (partRanges.destination == Destination.Rejected) {
        // Just throw this one out
        continue
      } else if (partRanges.destination == Destination.Nope) {
        throw Exception("wtf")
      }

      val destination = partRanges.destination as Destination.NewRule

      // Find the next workflow
      val workflow = workflows.getValue(destination.name)

      workflow.rules.forEach { rule ->
        when (rule) {
          is Rule.Unconditional -> {
            when (val destination = rule.destination) {
              Destination.Accepted -> accepted.add(partRanges.copy(destination = destination))
              Destination.Rejected -> {
                // all bad parts!
              }
              is Destination.NewRule -> {
                unresolvedParts.add(PartRanges(rule.destination, partRanges.categoriesToRangeMaps))
              }
              else -> throw Exception("Expected a real destination here!")
            }
          }
          is Rule.GreaterThan -> {
            val oldRange = partRanges.categoriesToRangeMaps.getValue(rule.category)
            val passThroughRange = oldRange.first..rule.operand
            val redirectedRange = (rule.operand + 1)..oldRange.last

            if (!redirectedRange.isEmpty()) {
              unresolvedParts.add(
                PartRanges(
                  rule.destination,
                  partRanges.categoriesToRangeMaps.toMutableMap().apply { set(rule.category, redirectedRange) }
                )
              )
            }

            if (!passThroughRange.isEmpty()) {
              partRanges = PartRanges(
                Destination.Nope,
                partRanges.categoriesToRangeMaps.toMutableMap().apply { set(rule.category, passThroughRange) }
              )
            }

          }
          is Rule.LessThan -> {
            val oldRange = partRanges.categoriesToRangeMaps.getValue(rule.category)
            val passThroughRange = (rule.operand + 1)..oldRange.last
            val redirectedRange = oldRange.first..rule.operand

            if (!redirectedRange.isEmpty()) {
              unresolvedParts.add(
                PartRanges(
                  rule.destination,
                  partRanges.categoriesToRangeMaps.toMutableMap().apply { set(rule.category, redirectedRange) }
                )
              )
            }

            if (!passThroughRange.isEmpty()) {
              partRanges = PartRanges(
                Destination.Nope,
                partRanges.categoriesToRangeMaps.toMutableMap().apply { set(rule.category, passThroughRange) }
              )
            }
          }
        }
      }
    }

    println(accepted.joinToString("\n"))

    val values = accepted.map { (_, rangeMaps) ->
      rangeMaps.values.fold(1L) { acc, range ->
        ((range.last - range.first) + 1) * acc
      }
    }

    println(values)

    // TODO: this is counting the merged options twice.  Need to intersect the ranges...
    println(values.sum())
    // Answer was too high for the test data
    // 167409079868000
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

private fun IntRange.rangeIntersection(passThroughRange: IntRange): Triple<IntRange, IntRange, IntRange> {
  // Which part of this range pass one
  TODO("Not yet implemented")
}

private fun String.toPart(): Day.Part {
  return trim('{', '}').split(',')
    .map {
      it.split('=').let { x ->
        x.first().first() to x.last().toInt()
      }
    }.let { Day.Part(it) }
}

private fun String.toWorkflow(): Pair<String, Day.Workflow> {
  val name = substringBefore('{')
  val rulesString = substringBetween('{', '}')
  val rules = rulesString.split(',').map { it.toRule() }

  return name to Day.Workflow(rules)
}

private fun String.toRule(): Day.Rule {
  val parts = split(':')
  return if (parts.size == 1) {
    Day.Rule.Unconditional(parts.first().toDestination())
  } else {
    val destination = parts.last().toDestination()
    parts.first().toConditional(destination)
  }
}

private fun String.toConditional(destination: Day.Destination): Day.Rule {
  val category = this.first()
  val operator = this[1]
  val operand = this.substring(2).toInt()

  return when (operator) {
    '<' -> Day.Rule.LessThan(category, operand, destination)
    '>' -> Day.Rule.GreaterThan(category, operand, destination)
    else -> throw Exception("Bad rule: $this")
  }
}

private fun String.toDestination() = when (this) {
  "A" -> Day.Destination.Accepted
  "R" -> Day.Destination.Rejected
  else -> Day.Destination.NewRule(this)
}

private fun String.substringBetween(start: Char, end: Char): String {
  return substringAfter(start).substringBefore(end)
}
