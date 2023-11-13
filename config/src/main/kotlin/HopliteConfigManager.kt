package com.atombuilt.atomkt.config

import com.atombuilt.atomkt.commons.string.appendPrefixIfAbsent
import com.sksamuel.hoplite.ClasspathResourceLoader.Companion.toClasspathResourceLoader
import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.ConfigSource
import com.sksamuel.hoplite.addPathSource
import com.sksamuel.hoplite.addResourceSource
import com.sksamuel.hoplite.fp.getOrElse
import org.koin.core.Koin
import org.koin.core.module.Module
import org.koin.dsl.bind
import org.koin.dsl.binds
import org.koin.dsl.module
import java.nio.file.Path
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentMap
import kotlin.io.path.createParentDirectories
import kotlin.io.path.exists
import kotlin.io.path.writeBytes
import kotlin.reflect.KClass

internal class HopliteConfigManager(
    private val koin: Koin,
    private val classLoader: ClassLoader
) : ConfigManager {

    private val config: ConcurrentMap<String, Any> = ConcurrentHashMap()
    private val declarations: ConcurrentMap<String, ConfigDeclaration> = ConcurrentHashMap()
    private val module: Module = module(createdAtStart = true) {
        single { this@HopliteConfigManager } bind ConfigManager::class
    }

    override fun load() {
        loadConfig()
        koin.loadModule(module)
    }

    override fun unload() {
        koin.unloadModule(module)
        config.clear()
        declarations.clear()
    }

    override fun reload() {
        loadConfig()
    }

    override fun <T : Any> declareConfigByPath(configKClass: KClass<T>, path: Path) {
        val configLoader = configLoaderBuilder().addPathSource(path).build()
        val configSources = listOf(ConfigSource.PathSource(path))
        val configKey = configKClass.qualifiedName!!
        declarations[configKey] = ConfigDeclaration(configKClass, configLoader, configSources)
        module.factory { retrieveConfig(configKey) } binds arrayOf(configKClass)
    }

    override fun <T : Any> declareConfigByClasspath(configKClass: KClass<T>, classpath: String) {
        val classpathWithPrefix = classpath.appendPrefixIfAbsent("/")
        val configLoader = configLoaderBuilder().addResourceSource(classpathWithPrefix).build()
        val configSources = listOf(ConfigSource.ClasspathSource(classpath, classLoader.toClasspathResourceLoader()))
        val configKey = configKClass.qualifiedName!!
        declarations[configKey] = ConfigDeclaration(configKClass, configLoader, configSources)
        module.factory { retrieveConfig(configKey) } binds arrayOf(configKClass)
    }

    override fun <T : Any> declareConfigByPathOrClasspath(configKClass: KClass<T>, path: Path, classpath: String) {
        val classpathWithPrefix = classpath.appendPrefixIfAbsent("/")
        val configLoader = configLoaderBuilder()
            .addPathSource(path, optional = true, allowEmpty = true)
            .addResourceSource(classpathWithPrefix)
            .build()
        val configSources = listOf(
            ConfigSource.PathSource(path),
            ConfigSource.ClasspathSource(classpath.removePrefix("/"), classLoader.toClasspathResourceLoader())
        )
        val configKey = configKClass.qualifiedName!!
        declarations[configKey] = ConfigDeclaration(configKClass, configLoader, configSources)
        module.factory { retrieveConfig(configKey) } binds arrayOf(configKClass)
    }

    private fun retrieveConfig(key: String): Any {
        val cache = config[key]
        if (cache != null) return cache
        val declaration = declarations[key]
        require(declaration != null) { "Declaration for config $key not found." }
        return loadConfig(declaration)
    }

    private fun configLoaderBuilder() = ConfigLoader.builder().withClassLoader(classLoader).addDefaults()

    private fun loadConfig() {
        declarations.forEach { (key, declaration) ->
            config[key] = loadConfig(declaration)
        }
    }

    private fun loadConfig(declaration: ConfigDeclaration): Any {
        val (kClass, loader, sources) = declaration
        if (sources.size == 2) writeDefaultsIfNotExists(sources[0], sources[1])
        return loader.loadConfigOrThrow(kClass, emptyList())
    }

    private fun writeDefaultsIfNotExists(to: ConfigSource, from: ConfigSource) {
        if (to !is ConfigSource.PathSource || to.path.exists()) return
        from.open(false).getOrElse { null }?.use { inputStream ->
            to.path.createParentDirectories()
            to.path.writeBytes(inputStream.readAllBytes())
        }
    }

    private fun Koin.loadModule(module: Module) = loadModules(Collections.singletonList(module))

    private fun Koin.unloadModule(module: Module) = unloadModules(Collections.singletonList(module))

    private data class ConfigDeclaration(
        val kClass: KClass<*>,
        val loader: ConfigLoader,
        val sources: List<ConfigSource>,
    )
}
