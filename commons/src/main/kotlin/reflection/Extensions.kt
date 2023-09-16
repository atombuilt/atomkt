package com.atombuilt.atomkt.commons.reflection

import java.lang.reflect.AccessibleObject

/**
 * Assure an unchecked cast.
 * An alternative to just writing `any as T` that does not give a warning.
 * @receiver The instance to assure as type T.
 * @return The instance assured as type T.
 */
@Suppress("UNCHECKED_CAST")
public fun <T : Any> Any.assure(): T = this as T

/**
 * Changes the accessibility of the receiver to true until the block is finished.
 * Then changes it back to the original state.
 */
public inline fun <T : AccessibleObject, R : Any> T.access(block: T.() -> R): R {
    val originalAccessState = @Suppress("DEPRECATION") isAccessible
    isAccessible = true
    val result = block()
    isAccessible = originalAccessState
    return result
}
