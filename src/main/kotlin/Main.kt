import aoc2022.day06.appMain
import aoc2022.day06.Day
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
        part1b()
        part2b()

        // val trials = 1000
        //
        // println(measureTimeMillis { repeat(trials) { part1() } })
        // println()
        // println(measureTimeMillis {  repeat(trials) { part2() } })
        // println()
        // println(measureTimeMillis {  repeat(trials) { part1b() } })
        // println()
        // println(measureTimeMillis {  repeat(trials) { part2b() } })
        // println()

      }
    }
  }
}
