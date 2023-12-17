package com.atombuilt.atomkt.commons.string

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
