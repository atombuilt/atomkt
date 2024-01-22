package com.atombuilt.atomkt.spigot

import com.atombuilt.atomkt.config.ConfigManager
import com.atombuilt.atomkt.config.loadConfigManager
import com.atombuilt.atomkt.spigot.component.KotlinPluginComponent
import com.atombuilt.atomkt.spigot.coroutines.PluginAsyncCoroutineDispatcher
import com.atombuilt.atomkt.spigot.coroutines.PluginCoroutineDispatcher
import com.atombuilt.atomkt.spigot.coroutines.ServerHeartbeatController
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.get
import org.koin.core.context.GlobalContext
import org.koin.core.module.Module
import org.koin.dsl.binds
import org.koin.dsl.module
import kotlin.coroutines.CoroutineContext

/**
 * Abstraction over [JavaPlugin] with support for Kotlin features,
 * such as coroutines.
 */
public abstract class KotlinPlugin : JavaPlugin(), KoinComponent {

    private val internalModule: Module = module(createdAtStart = true) {
        single { this@KotlinPlugin } binds arrayOf(KotlinPlugin::class, JavaPlugin::class, Plugin::class)
    }
    private val serverHeartbeatController by lazy { ServerHeartbeatController(this) }
    private val coroutineDispatcher by lazy { PluginCoroutineDispatcher(serverHeartbeatController, this) }
    private val asyncCoroutineDispatcher by lazy { PluginAsyncCoroutineDispatcher(this) }
    private val components: MutableSet<KotlinPluginComponent> = mutableSetOf()
    private var hasProcessedComponents: Boolean = false

    /**
     * The primary coroutine context of the plugin.
     * The coroutines inside of this context are running on the server's primary thread.
     * Can be used only while the plugin is enabled, otherwise just ignores the coroutines.
     */
    public lateinit var coroutineContext: CoroutineContext
        private set

    /**
     * The primary coroutine scope of the plugin.
     * The coroutines inside of this scope are running on the server's primary thread.
     * Can be used only while the plugin is enabled, otherwise just ignores the coroutines.
     */
    public lateinit var coroutineScope: CoroutineScope
        private set

    /**
     * The async coroutine context of the plugin.
     * The coroutines inside of this context are running on an asynchronous thread.
     * Can be used only while the plugin is enabled, otherwise just ignores the coroutines.
     */
    public lateinit var asyncCoroutineContext: CoroutineContext
        private set

    /**
     * The async coroutine scope of the plugin.
     * The coroutines inside of this scope are running on an asynchronous thread.
     * Can be used only while the plugin is enabled, otherwise just ignores the coroutines.
     */
    public lateinit var asyncCoroutineScope: CoroutineScope
        private set

    /**
     * Kotlin's variant of the plugin logger.
     */
    public val log: KLogger = KotlinLogging.logger(name)

    /**~
     * Kotlin's variant of the plugin logger.
     */
    public val logger: KLogger get() = log

    /**
     * Module containing dependencies of this plugin.
     */
    protected open val module: Module = module {}

    final override fun onLoad() {
        runBlocking { onLoaded() }
    }

    /**
     * Called once the plugin is loaded on the server.
     */
    protected open suspend fun onLoaded() {
        // Has empty default implementation as is rarely used.
    }

    final override fun onEnable() {
        initCoroutines()
        serverHeartbeatController.runControlled {
            startKoin()
            processComponents()
            onEnabled()
            registerComponents()
        }
    }

    private suspend fun registerComponents() {
        components.forEach { it.registerComponent(this) }
    }

    private fun initCoroutines() {
        coroutineContext = createCoroutineContext("main", coroutineDispatcher)
        coroutineScope = CoroutineScope(coroutineContext)
        asyncCoroutineContext = createCoroutineContext("async", asyncCoroutineDispatcher)
        asyncCoroutineScope = CoroutineScope(asyncCoroutineContext)
    }

    private fun createCoroutineContext(name: String, dispatcher: CoroutineDispatcher): CoroutineContext {
        val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
            log.warn(throwable) { "An exception thrown in the $name coroutine context." }
        }
        return exceptionHandler + SupervisorJob() + dispatcher
    }

    private fun startKoin() {
        val koinApplication = GlobalContext.getKoinApplicationOrNull()
        if (koinApplication == null) {
            GlobalContext.startKoin {
                loadPluginConfigManager()
                modules(internalModule, module)
            }
        } else {
            koinApplication.loadPluginConfigManager()
            koinApplication.koin.loadModules(listOf(internalModule, module))
        }
    }

    private fun KoinApplication.loadPluginConfigManager() {
        val configManager = getKoin().getOrNull<ConfigManager>() ?: ConfigManager.default(koin, classLoader)
        loadConfigManager(configManager = configManager) { configuration() }
    }

    /**
     * Method for declaring the plugin configuration.
     */
    protected open fun ConfigManager.configuration() {}

    /**
     * Declares a config in the plugin data dir backed up by the classpath default config.
     */
    protected inline fun <reified T : Any> ConfigManager.declarePluginConfig(name: String) {
        declareConfigByPathOrClasspath(T::class, dataFolder.resolve(name).toPath(), "/config/$name")
    }

    private suspend fun processComponents() {
        if (hasProcessedComponents) return
        hasProcessedComponents = true
        attachComponents()
        components.addAll(getKoin().getAll<KotlinPluginComponent>())
    }

    /**
     * Called once the plugin is enabled.
     */
    protected abstract suspend fun onEnabled()

    final override fun onDisable() {
        try {
            serverHeartbeatController.runControlled {
                unregisterComponents()
                onDisabled()
                stopKoin()
            }
        } finally {
            val cancellationException = CancellationException("Stopping the plugin.")
            coroutineScope.cancel(cancellationException)
            asyncCoroutineScope.cancel(cancellationException)
        }
    }

    private suspend fun unregisterComponents() {
        components.forEach { it.unregisterComponent(this) }
    }

    private fun stopKoin() {
        get<ConfigManager>().unload()
        getKoin().unloadModules(listOf(internalModule, module))
        GlobalContext.stopKoin()
    }

    /**
     * Called once the plugin is disabled.
     */
    protected abstract suspend fun onDisabled()

    /**
     * This method is called only once in the plugin's lifecycle before [onEnabled].
     * It is used for attaching components to the plugin's lifecycle.
     * @see KotlinPluginComponent
     * @see attachComponent
     * @see detachComponent
     */
    protected open suspend fun attachComponents() {
        // Has empty default implementation to be overridden.
    }

    /**
     * Attach [component] to this plugin's lifecycle.
     * @param component The component to attach.
     * @see KotlinPluginComponent
     * @see detachComponent
     */
    public fun attachComponent(component: KotlinPluginComponent) {
        components.add(component)
    }

    /**
     * Detach [component] from this plugin's lifecycle.
     *
     * @param component The component to detach.
     * @see KotlinPluginComponent
     * @see attachComponent
     */
    public fun detachComponent(component: KotlinPluginComponent) {
        components.remove(component)
    }

    final override fun onCommand(
        sender: CommandSender,
        command: Command,
        label: String,
        args: Array<out String>?
    ): Boolean {
        log.warn { "Method JavaPlugin#onCommand has been called, but is not supported by the Kotlin plugin abstraction." }
        return false
    }

    final override fun onTabComplete(
        sender: CommandSender,
        command: Command,
        alias: String,
        args: Array<out String>?
    ): MutableList<String>? {
        log.warn { "Method JavaPlugin#onTabComplete has been called but is not supported by the Kotlin plugin abstraction." }
        return null
    }
}
