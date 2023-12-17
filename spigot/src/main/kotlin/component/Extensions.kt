package com.atombuilt.atomkt.spigot.component

import org.koin.core.definition.Definition
import org.koin.core.module.Module
import org.koin.core.qualifier.Qualifier
import org.koin.dsl.binds

/**
 * Declares a [KotlinPluginComponent] as a singleton.
 * This will bind the component to the [KotlinPluginComponent] and [T] interfaces.
 * If declared in plugin's [module][com.atombuilt.atomkt.spigot.KotlinPlugin.module],
 * the component will be attached to the plugin automatically.
 */
public inline fun <reified T : KotlinPluginComponent> Module.pluginComponent(
    qualifier: Qualifier? = null,
    createdAtStart: Boolean = false,
    noinline definition: Definition<T>
) {
    single(qualifier, createdAtStart, definition) binds arrayOf(T::class, KotlinPluginComponent::class)
}
