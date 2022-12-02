package aoc2022.day02

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 2
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<Pair<Weapon, String>>
  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  data class Rule(val self: Weapon, val beats: Weapon, val losesTo: Weapon) {
    fun fight(other: Weapon) : Outcome {
      return when (other) {
        self -> Outcome.Draw
        beats -> Outcome.Win
        losesTo -> Outcome.Loss
        else -> throw Exception("WTF")
      }
    }
  }

  sealed class Weapon(val score: Int) {
    object Rock: Weapon(1) {
      private val _rule = Rule(Rock, Scissors, Paper)
      override fun getRule() = _rule
    }

    object Paper: Weapon(2) {
      private val _rule = Rule(Paper, Rock, Scissors)
      override fun getRule() = _rule
    }

    object Scissors: Weapon(3) {
      private val _rule = Rule(Scissors, Paper, Rock)
      override fun getRule() = _rule
    }

    abstract fun getRule(): Rule
  }

  private val letterToWeaponMap = mapOf(
    "A" to Weapon.Rock,
    "X" to Weapon.Rock,
    "B" to Weapon.Paper,
    "Y" to Weapon.Paper,
    "C" to Weapon.Scissors,
    "Z" to Weapon.Scissors,
  )

  sealed class Outcome(val score: Int) {
    object Win: Outcome(6)
    object Loss: Outcome(0)
    object Draw: Outcome(3)
  }

  val sampleInput = """
    A Y
    B X
    C Z""".trimIndent().split("\n").map { it.split(" ") }

  init {
  }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString().split("\n").map { it.split(" ") }
      realInput
    } else {
      sampleInput
    }.mapNotNull { round ->
      if (round.size == 2) {
        letterToWeaponMap[round.first()]!! to round.last()
      } else null
    }
  }

  private val symbolToResult = mapOf(
    "X" to Outcome.Loss,
    "Y" to Outcome.Draw,
    "Z" to Outcome.Win,
  )

  fun part1() {
    val score = input.sumOf { round ->
      val ourWeapon = letterToWeaponMap[round.second]!!
      val theirWeapon = round.first
      val weaponScore = ourWeapon.score
      val fightScore = ourWeapon.getRule().fight(theirWeapon).score
      weaponScore + fightScore
    }
    println(score)
  }

  fun part2() {
    val scores = input.map { round ->
      val theirWeapon = round.first
      val outcome = symbolToResult[round.second]!!
      val fightScore = outcome.score

      val ourWeapon = when (outcome) {
        Outcome.Draw -> theirWeapon
        Outcome.Loss -> theirWeapon.getRule().beats
        Outcome.Win -> theirWeapon.getRule().losesTo
      }

      val weaponScore = ourWeapon.score
      weaponScore + fightScore
    }
    // println(scores)
    println(scores.sum())
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