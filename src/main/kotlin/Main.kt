import aoc2022.day01.appMain
import aoc2022.day01.Day
import kotlinx.coroutines.runBlocking

fun main() {
  // appMain()

  runBlocking {
    Day(this).apply {
      useRealData = true
      initialize()
      part1()
      part2()
    }
  }
}
