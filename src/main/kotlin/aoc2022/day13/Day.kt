package aoc2022.day13

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import kotlin.math.sign
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
    val answer = input.mapIndexedNotNull { index, pair ->
      val left = parseTree(pair.first())
      val right = parseTree(pair.last())
      val c = left.compare(right)
      // println("$index:\n$left\n$right\n$c\n")
      if (c == -1) {
        index + 1
      } else {
        null
      }
    }.sum()
    println(answer)
  }

  fun part2() {
    val items = input.flatten().toMutableList()
    items.add("[[2]]")
    items.add("[[6]]")

    val sorted = items.map { parseTree(it) }.sortedWith((Comparator { o1, o2 -> o1.compare(o2) }))

    val dividers = listOf(
      parseTree("[[2]]"),
      parseTree("[[6]]")
    )

    val answer = sorted.withIndex().filter { it.value in dividers  }

    val m = answer.map { it.index + 1 }.fold(1) { a, b ->
      a * b
    }

    println(m)

    // println(sorted.withIndex().joinToString("\n") {
    //   "${it.index + 1}: ${it.value}"
    // })
  }

  private fun parseTree(str: String): Component {
    return parseTree(PeekingIterator(str)).list.first()
  }

  private fun parseTree(iter: PeekingIterator) : Component.CList {
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
  abstract fun compare(other: Component): Int

  data class CList(val list: MutableList<Component> = mutableListOf()) : Component() {
    constructor(items: Collection<Int>) : this(items.map { CInt(it) }.toMutableList())

    override fun toString(): String = list.toString()

    fun add(ci: Component) {
      list.add(ci)
    }

    override fun compare(other: Component): Int {
      return when(other) {
        is CList -> compareList(other)
        is CInt -> compareList(CList(mutableListOf(other)))
      }
    }

    private fun compareList(other: CList): Int {
      val iter = list.iterator()
      val oIter = other.list.iterator()

      while (iter.hasNext() && oIter.hasNext()) {
        val value = iter.next()
        val otherValue = oIter.next()

        val result = value.compare(otherValue)
        if (result != 0) {
          return result
        }
      }

      if (!iter.hasNext() && !oIter.hasNext()) {
        return 0
      }

      if (iter.hasNext()) {
        return 1
      }

      return -1
    }
  }

  data class CInt(val value: Int) : Component() {

    override fun compare(other: Component): Int {
      return when(other) {
        is CList -> { other.compare(this) * -1 }
        is CInt -> { (this.value - other.value).sign }
      }
    }

    override fun toString(): String = value.toString()
  }
  // data class CString(val value: String) : Component() {
  //   override fun toString(): String {
  //     return value
  //   }
  // }
}