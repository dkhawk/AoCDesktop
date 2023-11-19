import aoc2018.day02.appMain
import aoc2018.day02.Day
import kotlin.system.measureTimeMillis
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val desktopApp = args.contains("desktop")
  if (desktopApp) {
    appMain()
  } else {
    runBlocking {
      Day(this).apply {
        useRealData = args.contains("real")
        // useRealData = true
        initialize()

        val t1 = measureTimeMillis {
          // part1()
        }
        val t2 = measureTimeMillis {
          part2()
        }

        // println()
        // println(t1)
        // println(t2)


      }
    }
  }
}
