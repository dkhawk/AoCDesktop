import aoc2023.day25.Day
// import aoc2023.day14.appMain
// import aoc2018.day15.Day
import kotlinx.coroutines.runBlocking

fun main(args: Array<String>) {
  val desktopApp = args.contains("desktop")
  if (desktopApp) {
   // appMain()
  } else {
    runBlocking {
      Day(this).apply {
        useRealData = args.contains("real")
        initialize()

        if (args.contains("part1")) {
          part1()
        }
        if (args.contains("part2")) {
          part2()
        }
      }
    }
  }
}
