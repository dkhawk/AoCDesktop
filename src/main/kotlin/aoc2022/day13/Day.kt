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
    input.take(10).forEach {
      val left = parseTree(it.first())
      val right = parseTree(it.last())
      compareComponents(left, right)
    }
  }

  private fun compareComponents(left: Component, right: Component) {
    println("Comparing $left to $right")

    val lIter = (left as Component.CList).list.iterator()
    val rIter = (right as Component.CList).list.iterator()

    //
    // val l = lIter.next()
    // val r = rIter.next()
  }

  private fun parseTree(str: String): Component {
    return parseTree(PeekingIterator(str)).list.first()
  }

  private fun parseTree(iter: PeekingIterator) : Component.CList {
//    val items = mutableListOf<Component>()
    val component = Component.CList()

    while (iter.hasNext()) {
      val c = iter.peek()
      when {
        c.isDigit() ->  {
          val ci = parseInt(iter)
          component.add(ci)
        }
        c == '[' -> {
          iter.next()
          val cl = parseTree(iter)
          component.add(cl)
        }
        c == ']' -> {
          iter.next()
          return component
        }
        c == ',' -> {
          iter.next()
        }
        else -> throw Exception("WTF?")
      }
    }

    return component
  }

  private fun parseInt(iter: PeekingIterator): Component.CInt {
    val s = mutableListOf<Char>()
    while (iter.hasNext() && iter.peek().isDigit()) {
      s.add(iter.next())
    }
    val i = s.joinToString("").toInt()
    return Component.CInt(i)
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

private fun String.peekingIterator(): PeekingIterator {
  return PeekingIterator(this)
}

class PeekingIterator(val str: String) {
  var peekBuffer: Char? = null
  val iter = str.iterator()

  fun hasNext(): Boolean = (peekBuffer != null) || iter.hasNext()

  fun next(): Char {
    return peekBuffer?.let {
      peekBuffer = null
      it
    } ?: iter.next()
  }

  fun peek(): Char {
    if (peekBuffer == null) {
      peekBuffer = iter.next()
    }

    return peekBuffer!!
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
  data class CList(val list: MutableList<Component> = mutableListOf()) : Component() {
    override fun toString(): String = list.toString()
    fun add(ci: Component) {
      list.add(ci)
    }
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