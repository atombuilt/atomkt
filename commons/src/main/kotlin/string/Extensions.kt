package com.atombuilt.atomkt.commons.string

public fun String.appendPrefixIfAbsent(prefix: String, ignoreCase: Boolean = false): String {
    return if (this.startsWith(prefix, ignoreCase)) this else prefix + this
}

public fun String.appendSuffixIfAbsent(suffix: String, ignoreCase: Boolean = false): String {
    return if (this.endsWith(suffix, ignoreCase)) this else this + suffix
}
