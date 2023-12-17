package com.atombuilt.atomkt.commons.locker

/**
 * Represents a locker.
 */
public interface Locker {

    /**
     * Locks the [key].
     * @return true if the [key] was locked successfully.
     */
    public fun lock(key: Any): Boolean

    /**
     * Unlocks the [key].
     * @return true if the [key] was unlocked successfully.
     */
    public fun unlock(key: Any): Boolean

    public companion object {

        /**
         * Creates a default implementation of [Locker].
         */
        public fun createDefault(): Locker = ConcurrentHashMapLocker()
    }
}
