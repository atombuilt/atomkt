package com.atombuilt.atomkt.config

import org.koin.core.KoinApplication
import java.nio.file.Path

/**
 * Creates a config manager linked to this [KoinApplication].
 */
public inline fun KoinApplication.loadConfigManager(
    classLoader: ClassLoader = ClassLoader.getSystemClassLoader(),
    configManager: ConfigManager = ConfigManager.default(koin, classLoader),
    block: ConfigManager.() -> Unit
) {
    configManager.apply(block).load()
}

/**
 * Declares a config by its file path.
 */
public inline fun <reified T : Any> ConfigManager.declareConfigByPath(path: Path) {
    declareConfigByPath(T::class, path)
}

/**
 * Declares a config by its classpath path.
 */
public inline fun <reified T : Any> ConfigManager.declareConfigByClasspath(path: String) {
    declareConfigByClasspath(T::class, path)
}

/**
 * Declares a config with optional file path and classpath path.
 */
public inline fun <reified T : Any> ConfigManager.declareConfigByPathOrClasspath(path: Path, classpath: String) {
    declareConfigByPathOrClasspath(T::class, path, classpath)
}
