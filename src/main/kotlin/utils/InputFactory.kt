package utils

import kotlin.reflect.KClass
import kotlin.reflect.KParameter
import kotlin.reflect.javaType

annotation class Template(val template: String)
annotation class Unsigned
annotation class Signed
annotation class AlphaOnly
annotation class Remaining
annotation class Custom(val pattern: String)

@kotlin.ExperimentalStdlibApi
class InputFactory(private val inputClass: KClass<*>) {
  private val attributeMap: Map<String, Mapper>
  private val regex: Regex
  private val constructor = inputClass.constructors.first()

  init {
    val template = (inputClass.annotations.find { it.annotationClass == Template::class } as Template).template

    val attributes = constructor.parameters.map { parameter ->
      parameterToRegex(parameter)
    }

    val expressions = attributes.map { it.expression }
    var regexPattern = template
    expressions.forEachIndexed { index, expression ->
      regexPattern = regexPattern.replace("#$index", expression)
    }
    regex = Regex(regexPattern)
    attributeMap = attributes.map { Pair(it.name, it) }.toMap()
  }

  fun <T> lineToClass(line: String) : T? {
    return regex.matchEntire(line)?.let { matchResult ->
      val argParams = attributeMap.mapNotNull { (k, v) ->
        matchResult.groups[k]?.value?.let {
          v.parameter to v.function.invoke(it)
        }
      }.toMap()
      constructor.callBy(argParams) as T
    }
  }



//  fun <T> l2c(line:String): T? {
//    val thing = regex.matchEntire(line)?.let { matchResult ->
//      val argParams = attributeMap.mapNotNull { (k, v) ->
//        matchResult.groups[k]?.value?.let {
//          v.parameter to v.function.invoke(it)
//        }
//      }.toMap()
//      constructor.callBy(argParams)
//    }
//
//    val t = inputClass.safeCast(thing)
//
//    return null
//  }


  companion object {
    @kotlin.ExperimentalStdlibApi
    fun parameterToRegex(parameter: KParameter): Mapper {
      val name = parameter.name!!

      val signed = parameter.annotations.find { it.annotationClass == Signed::class } != null
      val alphaOnly = parameter.annotations.find { it.annotationClass == AlphaOnly::class } != null
      val allRemaining = parameter.annotations.find { it.annotationClass == Remaining::class } != null
      val custom: Regex? = parameter.annotations.find { it.annotationClass == Custom::class }?.let { annotation -> getCustomRe(annotation) }

      val expr = custom ?: when (parameter.type.javaType) {
        Int::class.java -> if (signed) """[+-]?\d+""" else """\d+"""
        Long::class.java -> if (signed) """[+-]?\d+""" else """\d+"""
        String::class.java -> {
          if (allRemaining) """.*"""
          else if (alphaOnly) """[a-zA-Z]+""" else """\S+"""
        }
        Char::class.java -> """\w"""
        else -> ".+"
      }

      val function = when (parameter.type.javaType) {
        Int::class.java -> String::toInt
        Long::class.java -> String::toLong
        String::class.java -> String::toString
        Char::class.java -> String::first
        else -> String::toString
      }

      return Mapper(name, parameter, "(?<$name>$expr)", function)
    }

    private fun getCustomRe(annotation: Annotation): Regex {
      val c = annotation as Custom
      return Regex(c.pattern)
    }

//    fun parameterToRegex(parameter: Parameter): Mapper {
//      val name = parameter.name!!
//
//      val expr = when (parameter.type) {
//        Int::class.java -> """[+-]?\d+"""
//        Long::class.java -> """[+-]?\d+"""
//        String::class.java -> """\S+"""
//        Char::class.java -> """\w"""
//        else -> ".+"
//      }
//
//      val function = when (parameter.type) {
//        Int::class.java -> String::toInt
//        Long::class.java -> String::toLong
//        String::class.java -> String::toString
//        Char::class.java -> String::first
//        else -> String::toString
//      }
//
//      return Mapper(name, parameter, "(?<$name>$expr)", function)
//    }
  }

  data class Mapper(
    val name: String,
    val parameter: KParameter,
    val expression: String,
    val function: (String) -> Any
  )
}
