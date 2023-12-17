package com.atombuilt.atomkt.commons.locker

/**
 * Executes the [block] if the [key] is not locked, otherwise executes the [onLocked] block.
 * @return true if the [block] was executed.
 */
public inline fun Locker.runLocked(
    key: Any,
    onLocked: () -> Unit,
    block: () -> Unit
): Boolean {
    if (lock(key)) {
        try {
            block()
        } finally {
            unlock(key)
        }
        return true
    }
    onLocked()
    return false
}

/**
 * Executes the [block] if the [key] is not locked, otherwise does nothing.
 * @return true if the [block] was executed.
 */
public inline fun Locker.runLockedOrIgnore(key: Any, block: () -> Unit): Boolean {
    if (lock(key)) {
        try {
            block()
        } finally {
            unlock(key)
        }
        return true
    }
    return false
}

/**
 * Executes the [block] if the [key] is not locked, otherwise throws an error.
 * @return true if the [block] was executed.
 * @throws IllegalStateException if the [block] was not executed.
 */
public inline fun Locker.runLockedOrThrow(key: Any, block: () -> Unit): Boolean {
    if (lock(key)) {
        try {
            block()
        } finally {
            unlock(key)
        }
        return true
    }
    error("Failed to lock $key.")
}
