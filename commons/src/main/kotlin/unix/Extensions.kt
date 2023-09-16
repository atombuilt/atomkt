package com.atombuilt.atomkt.commons.unix

/**
 * Returns a [Long] number representing current UNIX timestamp.
 */
public val unixTimestamp: Long get() = System.currentTimeMillis() / 1000
