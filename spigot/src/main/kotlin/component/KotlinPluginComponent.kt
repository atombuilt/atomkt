package com.atombuilt.atomkt.spigot.component

import com.atombuilt.atomkt.spigot.KotlinPlugin
import org.koin.core.component.KoinComponent

/**
 * Represents a [KotlinPlugin] component.
 * @see KotlinPlugin
 */
public interface KotlinPluginComponent : KoinComponent {

    /**
     * Registers this component to the [plugin].
     * @return true if the component was registered successfully.
     */
    public suspend fun registerComponent(plugin: KotlinPlugin): Boolean

    /**
     * Unregisters this component from the [plugin].
     * @return true if the component was unregistered successfully.
     */
    public suspend fun unregisterComponent(plugin: KotlinPlugin): Boolean
}
