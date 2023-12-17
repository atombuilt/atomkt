@file:Suppress("unused")

package com.atombuilt.atomkt.spigot.listener

import com.atombuilt.atomkt.spigot.KotlinPlugin
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

public inline fun <reified T : Event> KotlinPlugin.attachListener(
    noinline block: T.() -> Unit
): KotlinListener = object : KotlinListener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    fun eventHandler(event: T) = block(event)
}.also { attachComponent(it) }

public inline fun <reified T : Event> listener(
    noinline block: T.() -> Unit
): KotlinListener = object : KotlinListener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    fun eventHandler(event: T) = block(event)
}
