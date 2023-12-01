package com.atombuilt.atomkt.config

import com.sksamuel.hoplite.ConfigLoaderBuilder
import org.koin.core.Koin
import java.nio.file.Path
import kotlin.reflect.KClass

/**
 * Represents a configuration manager.
 */
public interface ConfigManager {

    /**
     * Loads the configuration into memory and exposes the config module to koin.
     */
    public fun load()

    /**
     * Unloads the config module from koin and cleans up the configuration from memory.
     */
    public fun unload()

    /**
     * Reloads the configuration into memory and re-exposes the config module to koin.
     */
    public fun reload()

    /**
     * Declares a config by its file path.
     */
    public fun <T : Any> declareConfigByPath(configKClass: KClass<T>, path: Path)

    /**
     * Declares a config by its classpath path.
     */
    public fun <T : Any> declareConfigByClasspath(configKClass: KClass<T>, classpath: String)

    /**
     * Declares a config with optional file path and classpath path.
     */
    public fun <T : Any> declareConfigByPathOrClasspath(configKClass: KClass<T>, path: Path, classpath: String)

    public companion object {

        /**
         * Creates an instance of the default implementation for [ConfigManager].
         */
        public fun default(koin: Koin, classLoader: ClassLoader): ConfigManager {
            return HopliteConfigManager(koin, classLoader)
        }

        /**
         * Creates an instance of the Hoplite implementation for [ConfigManager].
         */
        public fun hoplite(
            koin: Koin,
            classLoader: ClassLoader,
            configure: ConfigLoaderBuilder.() -> Unit
        ): ConfigManager {
            return HopliteConfigManager(koin, classLoader, configure)
        }
    }
}
