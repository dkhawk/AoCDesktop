package utils

import kotlin.math.sqrt

class Primes {
  val sieve = ArrayList<Int>(2000000)

  init {
    sieve.add(2)
  }

  fun until(maxPrime: Int) {
    while (sieve.last() <= maxPrime) {
      sieve.last().nextPrime()
    }
  }

  private fun Int.nextPrime(): Int {
    if (this < 2) {
      return 1
    }

    var target = this + 1
    while (true) {
      val max = sqrt(target.toDouble()).toInt()
      val f = sieve.filter { it <= max }.firstOrNull { target % it == 0 }
      if (f == null) {
        sieve.add(target)
        return target
      } else {
        target += 1
      }
    }
  }
}
