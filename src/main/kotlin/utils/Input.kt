package utils

import java.io.File

class Input {
  companion object {
    private const val YEAR = 2021
    private const val PATH = "/Users/dkhawk/Downloads/$YEAR/"

    fun readAsLines(day: Int, filterBlankLines: Boolean = true): List<String> =
      readFile(inputFileName(dayIntToString(day)), filterBlankLines)

    fun readAsLines(day: String, filterBlankLines: Boolean = true): List<String> =
      readFile(inputFileName(day), filterBlankLines)

    fun readAsString(day: String): String {
      return File(PATH + inputFileName(day)).readText()
    }

    private fun readFile(baseFilename: String, filterBlankLines: Boolean = true): List<String> =
      File(PATH + baseFilename).readLines().filter { if (filterBlankLines) it.isNotBlank() else true }

    private fun dayIntToString(day: Int) = day.toString().padStart(2, '0')

    private fun inputFileName(day: String): String = "input-$day.txt"
  }
}

class InputNew(private val year: Int, private val day: Int) {
  private val path = "/Users/dkhawk/IdeaProjects/aocDesktop/src/main/resources"

  private val inputFileName = "$path/$year/${day.toString().padStart(2, '0')}.txt"

  fun readAsLines(filterBlankLines: Boolean = true): List<String> =
    File(inputFileName).readLines().filter { if (filterBlankLines) it.isNotBlank() else true }

  fun readAsString(): String = File(inputFileName).readText()
}