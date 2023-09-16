package com.atombuilt.atomkt.commons.placeholder

private const val PLACEHOLDER_FORMAT = "{%s}"

/**
 * Type alias for placeholder pair.
 */
public typealias Placeholder = Pair<String, Any>
private typealias FormattedPlaceholder = Pair<String, String>

/**
 * Set placeholders to array strings.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New array with placeholders set.
 */
public fun Array<String>.setPlaceholders(
    vararg placeholders: Placeholder,
    format: String = PLACEHOLDER_FORMAT
): Array<String> {
    return setPlaceholders(placeholders.asList(), format)
}

/**
 * Set placeholders to array strings.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New array with placeholders set.
 */
public fun Array<String>.setPlaceholders(
    placeholders: Collection<Placeholder>,
    format: String = PLACEHOLDER_FORMAT
): Array<String> {
    if (isEmpty() || placeholders.isEmpty()) return this
    val formattedPlaceholders = placeholders.format(format)
    val buffer = Array(size) { "" }
    forEachIndexed { index, string ->
        buffer[index] = string.setFormattedPlaceholders(formattedPlaceholders)
    }
    return buffer
}

/**
 * Set placeholders to collection of strings.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New collection with placeholders set.
 */
public fun Collection<String>.setPlaceholders(
    vararg placeholders: Placeholder,
    format: String = PLACEHOLDER_FORMAT
): List<String> {
    return setPlaceholders(placeholders.asList(), format)
}

/**
 * Set placeholders to collection of strings.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New collection with placeholders set.
 */
public fun Collection<String>.setPlaceholders(
    placeholders: Collection<Placeholder>,
    format: String = PLACEHOLDER_FORMAT
): List<String> {
    if (isEmpty() || placeholders.isEmpty()) return toList()
    val formattedPlaceholders = placeholders.format(format)
    return map { it.setFormattedPlaceholders(formattedPlaceholders) }
}

/**
 * Set placeholders to a string.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New string with placeholders set.
 */
public fun String.setPlaceholders(
    vararg placeholders: Placeholder,
    format: String = PLACEHOLDER_FORMAT
): String {
    return setPlaceholders(placeholders.asList(), format)
}

/**
 * Set placeholders to a string.
 * @param format Format of the placeholder. By default, {%s}.
 * @return New string with placeholders set.
 */
public fun String.setPlaceholders(
    placeholders: Collection<Placeholder>,
    format: String = PLACEHOLDER_FORMAT
): String {
    if (isEmpty() || placeholders.isEmpty()) return this
    val formattedPlaceholders = placeholders.format(format)
    return setFormattedPlaceholders(formattedPlaceholders)
}

private fun String.setFormattedPlaceholders(placeholders: Collection<FormattedPlaceholder>): String {
    var buffer = this
    placeholders.forEach { (placeholder, value) ->
        buffer = buffer.replace(placeholder, value)
    }
    return buffer
}

private fun Collection<Placeholder>.format(format: String): List<FormattedPlaceholder> {
    return map { format.format(it.first) to it.second.toString() }
}
