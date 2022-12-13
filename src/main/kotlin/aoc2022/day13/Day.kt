package aoc2022.day13

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import utils.InputNew

const val day = 13
const val year = 2022

class Day(private val scope: CoroutineScope) {
  var useRealData by mutableStateOf(false)
  private lateinit var input: List<List<String>>

  private var job: Job? = null
  var running by mutableStateOf(false)
  var delayTime by mutableStateOf( 500L)
  val maxDelay = 500L

  val sampleInput = """
    [1,1,3,1,1]
    [1,1,5,1,1]

    [[1],[2,3,4]]
    [[1],4]

    [9]
    [[8,7,6]]

    [[4,4],4,4]
    [[4,4],4,4,4]

    [7,7,7,7]
    [7,7,7]

    []
    [3]

    [[[]]]
    [[]]

    [1,[2,[3,[4,[5,6,7]]]],8,9]
    [1,[2,[3,[4,[5,6,0]]]],8,9]
  """.trimIndent().split("\n\n").map { it.split("\n") }

  fun initialize() {
    input = if (useRealData) {
      val realInput = InputNew(year, day).readAsString().split("\n\n").map { it.split("\n") }
      realInput
    } else {
      sampleInput
    }
  }


  fun part1() {
    // println(parseTree("[]"))
    println(parseTree("[1]"))

    // println(parseTree("[1,2,3]"))
    // val pt = ParserTree()

    // println(parse("[]"))


    // parseTree("[[]]")
    // input.map {
    //   val leftString = it.first()
    //   val rightString = it[1]
    //
    //   val left = parse(leftString)
    // }
  }

  // private fun parse(str: String): Component {
  //   // The outermost component is always a list!
  // }
  //
  // private fun parseTree(str: String): Component {
  //   println("parsing '$str'")
  //   var start = 0
  //   var depth = 0
  //   var index = 0
  //
  //   // if (str.first().isDigit())
  //
  //   return if (str.first() == '[') {
  //     if (str.last() != ']') throw Exception("WTF: $str")
  //     val list = str.substring(1, str.length - 1)
  //     println("list: $list")
  //     parseList(list)
  //   } else {
  //     Component.CInt(str.toInt())
  //   }
  //   //
  //   // while (index < str.length) {
  //   //   when (val c = str[index]) {
  //   //     '[' -> depth += 1
  //   //     ']' -> {
  //   //       depth -= 1
  //   //       if (depth == 0) {
  //   //         parseTree(str.substring(start + 1, index))
  //   //         start = index
  //   //       }
  //   //     }
  //   //     else -> {
  //   //       if (depth == 0) {
  //   //         println(str.substring(index))
  //   //       }
  //   //     }
  //   //   }
  //   //   index += 1
  //   // }
  // }
  //

  private fun parseTree(str: String): Component.CList {
    println("Parsing $str")
    val items = mutableListOf<Component>()

    var start = 0
    var depth = 0
    var index = 0

    while (index < str.length) {
      val c = str[index]
      when {
        c.isDigit() ->  {
          start = index
          while (index < str.length && str[index].isDigit()) { index += 1 }
          items.add(Component.CInt(str.substring(start, index).toInt()))
          start = index
        }
        str[index] == '[' -> {
          // Find the end of this list
          start = index
          depth = 1
          while (index < str.length) {
            if (str[index] == '[') {
              depth += 1
            } else if (str[index] == ']') {
              depth -= 1
              if (depth == 0) {
                val ss = str.substring(start, index)
                println("substring: '$ss'")
              }
            }
            index += 1
          }

          // start = index
          // depth = 1
          // while (index < str.length) {
          //   if (str[index] == '[') {
          //     depth += 1
          //   } else if (str[index] == ']') {
          //     depth -= 1
          //     if (depth == 0) {
          //       items.add(
          //         parseList(str.substring(start + 1, index))
          //       )
          //       break
          //     }
          //   }
          //   index += 1
          // }
        }
        str[index] == ',' -> {
          index += 1
        }
        else -> throw Exception("WTF: $index $str $start")
      }
    }
    return Component.CList(items)
  }

  // private fun parse(str: String): Component {
  //   var result: Component? = null
  //
  //   var index = 0
  //   var isList = false
  //
  //   while (index < str.length) {
  //     when {
  //       str[index] == '[' -> result.add(parse(str.substring(1)))
  //       str[index] == ']' -> {
  //         result.add(Component.CList(result.toList()))
  //       }
  //       str[index].isDigit() -> {
  //         val start = index
  //         while (index < str.length && str[index].isDigit()) {
  //           index++
  //         }
  //         val value = str.substring(start, index).toInt()
  //         result.add(Component.CInt(value))
  //       }
  //
  //       else -> {
  //         // Comma?
  //       }
  //     }
  //   }
  //
  //   return Component.CList(result)
  // }

  fun part2() {
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

// class ParserTree() {
//   var index = 0
//   var currentList: MutableList<Component>? = null
//   var result: Component? = null
//
//   fun parse(str: String) : Component {
//     val c = str[index]
//     when (c) {
//       '[' -> {
//         currentList = mutableListOf()
//         currentList!!.add(parse(str.substring(index + 1)))
//       }
//       ']' -> {
//         // finish the current list
//         index++
//       }
//       ',' -> {
//         // add to the current list
//         index++
//       }
//       else -> {
//         // must be a digit!!
//         val start = index
//         while (index < str.length && str[index].isDigit()) {
//           index++
//         }
//         val value = str.substring(start, index).toInt()
//         currentList!!.add(Component.CInt(value))
//       }
//     }
//   }
// }

sealed class Component {
  data class CList(val list: List<Component>) : Component() {
    override fun toString(): String = list.toString()
  }
  data class CInt(val value: Int) : Component() {
    override fun toString(): String = value.toString()
  }
  data class CString(val value: String) : Component() {
    override fun toString(): String {
      return value
    }
  }
}
