package com.atombuilt.atomkt.spigot.command

import com.atombuilt.atomkt.commons.reflection.access
import com.atombuilt.atomkt.commons.reflection.assure
import com.atombuilt.atomkt.spigot.KotlinPlugin
import kotlinx.coroutines.launch
import org.bukkit.Location
import org.bukkit.command.Command
import org.bukkit.command.CommandMap
import org.bukkit.command.CommandSender
import org.bukkit.command.SimpleCommandMap

/**
 * Abstraction over the [Command] with support for Kotlin features,
 * such as coroutines.
 */
public abstract class KotlinCommand(
    protected val plugin: KotlinPlugin,
    name: String,
    description: String = "",
    usageMessage: String = "/<command>",
    aliases: List<String> = emptyList(),
    permission: String? = null,
) : Command(name, description, usageMessage, aliases) {

    init {
        super.setPermission(permission)
    }

    final override fun execute(sender: CommandSender, commandLabel: String, args: Array<String>): Boolean {
        var isSuccess = false
        plugin.coroutineScope.launch {
            isSuccess = execute(sender, args.asList())
        }
        return isSuccess
    }

    /**
     * Executes the command, returning its success.
     */
    public abstract suspend fun execute(sender: CommandSender, args: List<String>): Boolean

    final override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<String>
    ): MutableList<String> = tabCompleteAdapter(sender, alias, args)

    final override fun tabComplete(
        sender: CommandSender,
        alias: String,
        args: Array<String>,
        location: Location?
    ): MutableList<String> = tabCompleteAdapter(sender, alias, args)

    private fun tabCompleteAdapter(
        sender: CommandSender,
        alias: String,
        args: Array<String>,
    ): MutableList<String> {
        var result: List<String> = DefaultTabComplete
        plugin.coroutineScope.launch {
            result = tabComplete(sender, args.asList())
        }
        if (result === DefaultTabComplete) return super.tabComplete(sender, alias, args)
        return result.toMutableList()
    }

    /**
     * Executed on tab completion for this command,
     * returning a list of options the player can tab through.
     */
    public open suspend fun tabComplete(
        sender: CommandSender,
        args: List<String>
    ): List<String> {
        return DefaultTabComplete
    }

    /**
     * Links this command to the [plugin].
     * @see [KotlinPlugin.linkCommand]
     */
    public fun link(): Boolean {
        return plugin.linkCommand(this)
    }

    /**
     * Unlinks this command from the [plugin].
     * @see [KotlinPlugin.unlinkCommand]
     */
    public fun unlink(): Boolean {
        return plugin.unlinkCommand(this)
    }

    /**
     * Registers the command.
     * @return If the registration was successful.
     */
    public fun register(): Boolean {
        val commandMap = reflectCommandMap()
        if (commandMap == null) {
            plugin.log.error { "Could not register the command /$name because of command map reflection failure." }
            return false
        }
        val isSuccess = commandMap.register(plugin.name, this)
        if (isSuccess) updateCommandForPlayers()
        return isSuccess
    }

    /**
     * Unregisters the command.
     * @return If the unregistration was successful.
     */
    public fun unregister(): Boolean {
        val commandMap = reflectCommandMap()
        if (commandMap == null) {
            plugin.log.error { "Could not unregister the command /$name because of command map reflection failure." }
            return false
        }
        val knownCommands = commandMap.reflectKnownCommands()
        if (knownCommands == null) {
            plugin.log.error { "Could not unregister the command /$name because of command map known commands reflection failure." }
            return false
        }
        val keysForRemoval = knownCommands.filterValues { it === this }.keys
        keysForRemoval.forEach { knownCommands.remove(it, this) }
        val hasRemoved = keysForRemoval.isNotEmpty()
        if (hasRemoved) updateCommandForPlayers()
        return hasRemoved
    }

    private fun CommandMap.reflectKnownCommands(): MutableMap<String, Command>? = runCatching {
        if (this !is SimpleCommandMap) return null
        val knownCommandsField = this::class.java.getDeclaredField("knownCommands")
        knownCommandsField.access { get(this@reflectKnownCommands) }.assure<MutableMap<String, Command>>()
    }.onFailure {
        plugin.log.warn(it) { "Failed to reflect the command map known commands." }
    }.getOrNull()

    private fun updateCommandForPlayers() {
        plugin.server.onlinePlayers.forEach { it.updateCommands() }
    }

    private fun reflectCommandMap(): CommandMap? = runCatching {
        val server = plugin.server
        val commandMapField = server::class.java.getDeclaredField("commandMap")
        commandMapField.access { get(server) }.assure<CommandMap>()
    }.onFailure {
        plugin.log.warn(it) { "Failed to reflect the server command map." }
    }.getOrNull()

    private object DefaultTabComplete : List<String> by emptyList()
}
