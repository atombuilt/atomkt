package com.atombuilt.atomkt.spigot.listener

import com.atombuilt.atomkt.commons.reflection.access
import com.atombuilt.atomkt.commons.reflection.assure
import com.atombuilt.atomkt.spigot.KotlinPlugin
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.launch
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.plugin.EventExecutor
import org.bukkit.plugin.RegisteredListener
import kotlin.reflect.KFunction
import kotlin.reflect.full.*
import kotlin.reflect.javaType

/**
 * Abstraction over [Listener] with support for Kotlin features,
 * such as coroutines.
 */
public interface KotlinListener : Listener

/**
 * Registers the listener to the [plugin].
 */
public fun KotlinListener.register(plugin: KotlinPlugin) {
    reflectEventHandlers(plugin).forEach { (eventClass, listeners) ->
        val handlerList = eventClass.reflectHandlerList()
        handlerList.registerAll(listeners)
    }
}

/**
 * Unregisters the listener.
 */
public fun KotlinListener.unregister() {
    HandlerList.unregisterAll(this)
}

/**
 * Links the listener to the [plugin].
 * @see [KotlinPlugin.linkListener]
 */
public fun KotlinListener.link(plugin: KotlinPlugin): Boolean {
    return plugin.linkListener(this)
}

/**
 * Unlinks the listener from the [plugin].
 * @see [KotlinPlugin.unlinkListener]
 */
public fun KotlinListener.unlink(plugin: KotlinPlugin): Boolean {
    return plugin.unlinkListener(this)
}

private class KotlinEventExecutor(
    private val plugin: KotlinPlugin,
    private val eventClass: Class<out Event>,
    private val eventHandler: KFunction<*>
) : EventExecutor {

    override fun execute(listener: Listener, event: Event) {
        if (!eventClass.isAssignableFrom(event::class.java)) return
        try {
            if (eventHandler.isSuspend) {
                plugin.coroutineScope.launch(start = CoroutineStart.UNDISPATCHED) {
                    eventHandler.callSuspend(listener, event)
                }
            } else {
                eventHandler.call(listener, event)
            }
        } catch (e: Exception) {
            plugin.log.warn(e) { "Event handler $eventHandler has thrown an exception while handling $event." }
        }
    }
}

private fun KotlinListener.reflectEventHandlers(plugin: KotlinPlugin): Map<Class<out Event>, List<RegisteredListener>> {
    val listeners = mutableMapOf<Class<out Event>, MutableList<RegisteredListener>>()
    val candidates = this::class.memberFunctions.filter { it.isPlainFunction() && it.hasSingleEventParameter() }
    candidates.forEach { eventHandler ->
        val annotation = eventHandler.findAnnotation<EventHandler>() ?: return@forEach
        val eventClass = eventHandler.valueParameters.single().type.javaType.assure<Class<out Event>>()
        val executor = KotlinEventExecutor(plugin, eventClass, eventHandler)
        val registeredListener =
            RegisteredListener(this, executor, annotation.priority, plugin, annotation.ignoreCancelled)
        listeners.computeIfAbsent(eventClass) { mutableListOf() }.add(registeredListener)
    }
    return listeners
}

private fun KFunction<*>.isPlainFunction(): Boolean {
    return !isExternal && !isInline && !isOperator && !isInfix
}

private fun KFunction<*>.hasSingleEventParameter(): Boolean {
    val singleParam = valueParameters.singleOrNull() ?: return false
    val type = singleParam.type
    return type.isSubtypeOf(Event::class.starProjectedType)
}

private fun Class<out Event>.reflectHandlerList(): HandlerList {
    val handlerListMethod = declaredMethods.find { it.name == "getHandlerList" }
    if (handlerListMethod != null) return handlerListMethod.access { invoke(this) }.assure()
    if (Event::class.java.isAssignableFrom(superclass)) {
        return superclass.assure<Class<out Event>>().reflectHandlerList()
    }
    error("Static method getHandlerList is missing for the class $this.")
}
