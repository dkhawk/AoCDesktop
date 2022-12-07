import aoc2022.day07.appMain
import aoc2022.day07.Day
import kotlin.system.measureTimeMillis
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
