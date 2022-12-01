import aoc2022.day01.appMain
import aoc2022.day01.Day
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val desktopApp = args.contains("desktop")
  if (desktopApp) {
    appMain()
  } else {
    runBlocking {
      Day(this).apply {
        useRealData = true
        initialize()
        part1()
        part2()
      }
    }
  }
}
