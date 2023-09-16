@file:Suppress("unused")

package com.atombuilt.atomkt.spigot.listener

import com.atombuilt.atomkt.spigot.KotlinPlugin
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority

public fun KotlinPlugin.registerListener(listener: KotlinListener) {
    listener.register(this)
}

public inline fun <reified T : Event> KotlinPlugin.registerListener(
    noinline block: T.() -> Unit
): KotlinListener = listener(block).apply { registerListener(this) }

public inline fun <reified T : Event> KotlinPlugin.linkListener(
    noinline block: T.() -> Unit
): KotlinListener = listener(block).apply { linkListener(this) }

public inline fun <reified T : Event> listener(
    noinline block: T.() -> Unit
): KotlinListener = object : KotlinListener {
    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = false)
    fun eventHandler(event: T) = block(event)
}
