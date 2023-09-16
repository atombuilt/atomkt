package com.atombuilt.atomkt.spigot.time

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import java.time.Duration as JavaDuration

/**
 * The value of this [Int] ticks expressed as a [Duration].
 */
public val Int.ticks: Duration get() = (this * 50).milliseconds

/**
 * The value of this [Long] ticks expressed as a [Duration].
 */
public val Long.ticks: Duration get() = (this * 50).milliseconds

/**
 * The value of this duration expressed as a [Long] number of ticks.
 */
public val Duration.inWholeTicks: Long get() = inWholeSeconds * 20

/**
 * The value of this [java.time.Duration] expressed as a [Long] number of ticks.
 */
public fun JavaDuration.toTicks(): Long = toSeconds() * 20
