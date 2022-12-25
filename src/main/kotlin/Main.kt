import aoc2022.day25.appMain
import aoc2022.day25.Day
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
        // println(measureTimeMillis {
          part2()
        // })
      }
    }
  }
}
