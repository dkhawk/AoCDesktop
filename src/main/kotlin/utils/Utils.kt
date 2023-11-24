package utils

fun List<Int>.range(): Pair<Int, Int> {
  return this.minOf { it } to this.maxOf { it }
}
