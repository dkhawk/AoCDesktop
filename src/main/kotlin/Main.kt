import aoc2022.day03.appMain
import aoc2022.day03.Day
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val desktopApp = args.contains("desktop")
  if (desktopApp) {
    appMain()
  } else {
    runBlocking {
      Day(this).apply {
        useRealData = false
        useRealData = true
        initialize()
        part1()
        part2()
      }
    }
  }
}
