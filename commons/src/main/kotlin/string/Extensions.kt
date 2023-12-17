package com.atombuilt.atomkt.commons.string

import kotlin.math.abs

public fun String.appendPrefixIfAbsent(prefix: String, ignoreCase: Boolean = false): String {
    return if (this.startsWith(prefix, ignoreCase)) this else prefix + this
}

public fun String.appendSuffixIfAbsent(suffix: String, ignoreCase: Boolean = false): String {
    return if (this.endsWith(suffix, ignoreCase)) this else this + suffix
}

/**
 * Returns true if this string starts with any of the [prefixes].
 * @param ignoreCase true to ignore case when comparing strings.
 * @return true if this string starts with any of the [prefixes].
 */
public fun String.startsWith(vararg prefixes: String, ignoreCase: Boolean = false): Boolean {
    return prefixes.any { this.startsWith(it, ignoreCase) }
}

/**
 * Returns true if this string ends with any of the [suffixes].
 * @param ignoreCase true to ignore case when comparing strings.
 * @return true if this string ends with any of the [suffixes].
 */
public fun String.endsWith(vararg suffixes: String, ignoreCase: Boolean = false): Boolean {
    return suffixes.any { this.endsWith(it, ignoreCase) }
}

/**
 * Returns true if this string contains any of the [substrings].
 * @param ignoreCase true to ignore case when comparing strings.
 * @return true if this string contains any of the [substrings].
 */
public fun String.contains(vararg substrings: String, ignoreCase: Boolean = false): Boolean {
    return substrings.any { this.contains(it, ignoreCase) }
}

/**
 * Returns roman numeral representation of this integer.
 */
public fun Int.toRomanNumeral(): String {
    var input = abs(this)
    val builder = StringBuilder()

    fun extractUnit(perUnit: Int, representation: String) {
        if (input < perUnit) return
        repeat(input / perUnit) { builder.append(representation) }
        input %= perUnit
    }

    extractUnit(1000, "M")
    extractUnit(900, "CM")
    extractUnit(500, "D")
    extractUnit(400, "CD")
    extractUnit(100, "C")
    extractUnit(90, "XC")
    extractUnit(50, "L")
    extractUnit(40, "XL")
    extractUnit(10, "X")
    extractUnit(9, "IX")
    extractUnit(5, "V")
    extractUnit(4, "IV")
    extractUnit(1, "I")

    return builder.toString()
}
