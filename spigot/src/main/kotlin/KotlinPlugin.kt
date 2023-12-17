package com.atombuilt.atomkt.spigot

import com.atombuilt.atomkt.config.ConfigManager
import com.atombuilt.atomkt.config.loadConfigManager
import com.atombuilt.atomkt.spigot.command.KotlinCommand
import com.atombuilt.atomkt.spigot.coroutines.PluginAsyncCoroutineDispatcher
import com.atombuilt.atomkt.spigot.coroutines.PluginCoroutineDispatcher
import com.atombuilt.atomkt.spigot.coroutines.ServerHeartbeatController
import com.atombuilt.atomkt.spigot.listener.KotlinListener
import com.atombuilt.atomkt.spigot.listener.registerListener
import io.github.oshai.kotlinlogging.KLogger
import io.github.oshai.kotlinlogging.KotlinLogging
import kotlinx.coroutines.*
import org.bukkit.command.Command
import org.bukkit.command.CommandSender
import org.bukkit.event.HandlerList
import org.bukkit.plugin.Plugin
import org.bukkit.plugin.java.JavaPlugin
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
    private val linkedCommands: MutableSet<KotlinCommand> = HashSet()
    private val linkedListeners: MutableSet<KotlinListener> = HashSet()

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

    /**
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
            onEnabled()
            registerListeners()
            registerCommands()
        }
    }

    private fun registerListeners() {
        linkedListeners.forEach { registerListener(it) }
    }

    private fun registerCommands() {
        linkedCommands.filter { !it.isRegistered }.forEach { it.register() }
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
                loadConfigManager(classLoader = this::class.java.classLoader) { configuration() }
                modules(internalModule, module)
            }
        } else {
            koinApplication.loadConfigManager { configuration() }
            koinApplication.koin.loadModules(listOf(internalModule, module))
        }
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

    /**
     * Called once the plugin is enabled.
     */
    protected abstract suspend fun onEnabled()

    final override fun onDisable() {
        try {
            serverHeartbeatController.runControlled {
                unregisterListeners()
                unregisterCommands()
                onDisabled()
                stopKoin()
            }
        } finally {
            val cancellationException = CancellationException("Stopping the plugin.")
            coroutineScope.cancel(cancellationException)
            asyncCoroutineScope.cancel(cancellationException)
        }
    }

    private fun unregisterListeners() {
        HandlerList.unregisterAll(this)
    }

    private fun unregisterCommands() {
        linkedCommands.filter { it.isRegistered }.forEach { it.unregister() }
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
     * Links the [listener] to the plugin.
     * Linked listeners are registered once the plugin is enabled,
     * and unregistered once it is disabled.
     * If you link a listener after the plugin was enabled, it will be registered immediately.
     */
    public fun linkListener(listener: KotlinListener): Boolean {
        if (!linkedListeners.add(listener)) return false
        if (isEnabled) {
            HandlerList.unregisterAll(listener)
            registerListener(listener)
        }
        return true
    }

    /**
     * Unlinks the [listener] from the plugin.
     * Unregisters the listener if registered.
     */
    public fun unlinkListener(listener: KotlinListener): Boolean {
        if (!linkedListeners.remove(listener)) return false
        HandlerList.unregisterAll(listener)
        return true
    }

    /**
     * Links the [command] to the plugin.
     * Linked commands are registered once the plugin is enabled,
     * and unregistered once it is disabled.
     * If you link a command after the plugin was enabled, it will be registered immediately.
     */
    public fun linkCommand(command: KotlinCommand): Boolean {
        if (!linkedCommands.add(command)) return false
        if (isEnabled && !command.isRegistered) command.register()
        return true
    }

    /**
     * Unlinks the [command] from the plugin.
     * Unregisters the command if registered.
     */
    public fun unlinkCommand(command: KotlinCommand): Boolean {
        if (!linkedCommands.remove(command)) return false
        if (command.isRegistered) command.unregister()
        return true
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
