package com.atombuilt.atomkt.commons.locker

import java.util.*
import java.util.concurrent.ConcurrentHashMap

internal class ConcurrentHashMapLocker : Locker {

    private val set: MutableSet<Any> = Collections.newSetFromMap(ConcurrentHashMap())

    override fun lock(key: Any): Boolean = set.add(key)

    override fun unlock(key: Any): Boolean = set.remove(key)
}
