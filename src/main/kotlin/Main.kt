import aoc2022.day12.appMain
import aoc2022.day12.Day
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

        // part1()
        println(measureTimeMillis {
          part2()
        })

        initialize()
        println(measureTimeMillis {
          part2b()
        })
      }
    }
  }
}
