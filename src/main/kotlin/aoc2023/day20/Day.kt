package aoc2023.day20

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
    broadcaster -> a, b, c
    %a -> b
    %b -> c
    %c -> inv
    &inv -> a
  """.trimIndent().split("\n")

  val sampleInput2 = """
    broadcaster -> a
    %a -> inv, con
    &inv -> b
    %b -> con
    &con -> output
  """.trimIndent().split("\n")

  fun initialize() {
    input = if (useRealData) {
      val (year, day) = packageToYearDay(this.javaClass.packageName)
      val realInput = InputNew(year, day).readAsLines()
      realInput
    } else {
      sampleInput2
    }
  }

  enum class State {
    Off, On;

    operator fun not() = when (this) {
      Off -> On
      On -> Off
    }
  }

  enum class Pulse {
    Low, High
  }

  data class PulseEvent(val origin: String, val destination: String, val pulse: Pulse)

  sealed class Module(val destinations: List<String>) {
    abstract val name: String

    operator fun invoke(pulseEvent: PulseEvent) {
      invoke(pulseEvent.origin, pulseEvent.pulse)
    }
    abstract operator fun invoke(origin: String, pulse: Pulse)

    class FlipFlop(override val name: String, destinations: List<String>) : Module(destinations) {
      var state = State.Off
      /*
      Flip-flop modules (prefix %) are either on or off; they are initially off. If a flip-flop
      module receives a high pulse, it is ignored and nothing happens. However, if a flip-flop
      module receives a low pulse, it flips between on and off. If it was off, it turns on and sends
      a high pulse. If it was on, it turns off and sends a low pulse.
       */

      override operator fun invoke(origin: String, pulseIn: Pulse) {
        if (pulseIn == Pulse.Low) {
          when (state) {
            State.Off -> Pulse.High
            State.On -> Pulse.Low
          }.let { pulseOut ->
            destinations.forEach { destination -> sendPulse(PulseEvent(name, destination, pulseOut)) }
          }
          state = !state
        }
      }
    }

    class Conjunction(override val name: String, destinations: List<String>) : Module(destinations) {
      internal val rememberedPulses = mutableMapOf<String, Pulse>()

      fun initializeAllInputs(inputs: List<String>) {
        inputs.forEach {
          rememberedPulses[it] = Pulse.Low
        }
      }

      /*
      Conjunction modules (prefix &) remember the type of the most recent pulse received from each
      of their connected input modules; they initially default to remembering a low pulse for each
      input. When a pulse is received, the conjunction module first updates its memory for that
      input. Then, if it remembers high pulses for all inputs, it sends a low pulse; otherwise,
      it sends a high pulse.
       */
      override operator fun invoke(origin: String, pulse: Pulse) {
        rememberedPulses[origin] = pulse

        if (name == "ql" && pulse == Pulse.High) {
          println("$numberButtonPresses    $rememberedPulses")
        }

        val pulseOut = if (rememberedPulses.values.all { it == Pulse.High }) {
          Pulse.Low
        } else {
          Pulse.High
        }

        destinations.forEach { destination ->
          sendPulse(PulseEvent(name, destination, pulseOut))
        }
      }
    }

    class Broadcaster(destinations: List<String>) : Module(destinations) {
      override val name = "broadcaster"
      /*
      There is a single broadcast module (named broadcaster). When it receives a pulse, it sends
      the same pulse to all of its destination modules.
      */

      override operator fun invoke(origin: String, pulse: Pulse) {
        destinations.forEach { destination -> sendPulse(PulseEvent(name, destination, pulse)) }
      }
    }

    companion object {
      val pulseQueue = ArrayDeque<PulseEvent>()

      var numberButtonPresses = 0

      fun sendPulse(pulseEvent: PulseEvent) {
        /*
        Pulses are always processed in the order they are sent. So, if a pulse is sent to modules
        a, b, and c, and then module a processes its pulse and sends more pulses, the pulses sent to
        modules b and c would have to be handled first.
         */

        // "button -low-> broadcaster"

        // println("${pulseEvent.origin} -${pulseEvent.pulse}-> ${pulseEvent.destination}")

        pulseQueue.addLast(pulseEvent)
      }
    }
  }

  private lateinit var moduleMap: Map<String, Module>

  fun part1() {
    moduleMap = input.map { it.toModule() }.associateBy { it.name }

    // Find all inputs for all conjunction modules
    moduleMap.values.filterIsInstance<Module.Conjunction>().map { conjunction ->
      moduleMap.values.filter { it.destinations.contains(conjunction.name) }
        .map { it.name }
        .let { inputs -> conjunction.initializeAllInputs(inputs) }
    }

    // println(
    //   moduleMap.values.filterIsInstance<Module.Conjunction>()
    //     .joinToString("\n") { "${it.name} <- ${it.rememberedPulses}" })

    val totals = (0 until 1000).map {
      pushButton()
    }.reduce { acc, tot -> acc.zip(tot).map { (a, b) -> a + b } }

    println(totals)
    // println(totals.first() * totals.last())
  }

  private fun pushButton(): List<Int> {
    Module.sendPulse(PulseEvent("button", "broadcaster", Pulse.Low))

    var lowPulseCount = 0
    var highPulseCount = 0

    while (Module.pulseQueue.isNotEmpty()) {
      val pulseEvent = Module.pulseQueue.removeFirst()
      if (pulseEvent.pulse == Pulse.Low) lowPulseCount += 1 else highPulseCount += 1
      // println("d => '${pulseEvent.destination}'")
      val module = moduleMap[pulseEvent.destination]

      if (module != null) {
        module(pulseEvent)
      } else {
        // println("pulse sent to non-existing module: $pulseEvent")
      }
    }

    return listOf(lowPulseCount, highPulseCount)
  }

  val importantModules = listOf("fz", "mf", "fh", "ss")

  private fun pushButton2(): Boolean {
    Module.sendPulse(PulseEvent("button", "broadcaster", Pulse.Low))
    Module.numberButtonPresses += 1

    var completed = false

    while (Module.pulseQueue.isNotEmpty()) {
      val pulseEvent = Module.pulseQueue.removeFirst()
      val module = moduleMap[pulseEvent.destination]

      if (pulseEvent.destination == "rx" && pulseEvent.pulse == Pulse.Low) {
        completed = true
      }

      // if (pulseEvent.destination in importantModules) {
      //   println("$pulseEvent @ $numberButtonPresses")
      // }

      if (module != null) {
        module(pulseEvent)
      } else {
        // println("pulse sent to non-existing module: $pulseEvent")
      }
    }

    return completed
  }

  fun part2() {
    moduleMap = input.map { it.toModule() }.associateBy { it.name }

    // Find all inputs for all conjunction modules
    moduleMap.values.filterIsInstance<Module.Conjunction>().map { conjunction ->
      moduleMap.values.filter { it.destinations.contains(conjunction.name) }
        .map { it.name }
        .let { inputs -> conjunction.initializeAllInputs(inputs) }
    }

    // println(
    //   moduleMap.values.filterIsInstance<Module.Conjunction>()
    //     .joinToString("\n") { "${it.name} <- ${it.rememberedPulses}" })

    var count = 0L
    while (!pushButton2()) {
      count += 1
      if ((count % 100_000L) == 0L) {
        println(count)
      }
    }

    // 212986464842911

    println(count)
    // println(totals.first() * totals.last())

    // 212986.464842911
    // 8874.4360351212916666666666666666666666666666666666666666666666666...
    // 24 years
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

private fun String.toModule(): Day.Module {
  return when {
    startsWith('%') -> toFlipFlop()
    startsWith('&') -> toConjunction()
    startsWith("broadcaster") -> toBroadcaster()
    else -> throw Exception("Invalid module spec $this")
  }
}

fun String.toBroadcaster() : Day.Module.Broadcaster {
  val destinations = substringAfter("-> ").split(", ").map { it.trim() }
  return Day.Module.Broadcaster(destinations)
}

fun String.toConjunction() : Day.Module.Conjunction {
  val destinations = substringAfter("-> ").split(", ").map { it.trim() }
  val name = substring(1).substringBefore(' ')
  return Day.Module.Conjunction(name, destinations)
}

fun String.toFlipFlop() : Day.Module.FlipFlop {
  val destinations = substringAfter("-> ").split(", ").map { it.trim() }
  val name = substring(1).substringBefore(' ')
  return Day.Module.FlipFlop(name, destinations)
}
