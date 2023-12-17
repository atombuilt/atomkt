package com.atombuilt.atomkt.spigot.command

import com.atombuilt.atomkt.spigot.KotlinPlugin
import org.bukkit.command.CommandSender

public inline fun KotlinPlugin.attachCommand(
    name: String,
    block: KotlinCommandBuilder.() -> Unit
): KotlinCommand = command(name, block).also { attachComponent(it) }

public inline fun command(name: String, block: KotlinCommandBuilder.() -> Unit): KotlinCommand {
    val builder = KotlinCommandBuilder().apply(block)
    val executeBlock = builder.execute
    val tabCompleteBlock = builder.tabComplete
    return object : KotlinCommand(
        name = name,
        description = builder.description,
        usageMessage = builder.usageMessage,
        aliases = builder.aliases,
        permission = builder.permission
    ) {

        override suspend fun execute(sender: CommandSender, args: List<String>): Boolean {
            return if (executeBlock != null) {
                executeBlock(sender, args)
            } else false
        }

        override suspend fun tabComplete(sender: CommandSender, args: List<String>): List<String> {
            return if (tabCompleteBlock != null) {
                tabCompleteBlock(sender, args)
            } else super.tabComplete(sender, args)
        }
    }
}

public class KotlinCommandBuilder(
    public var execute: (suspend (sender: CommandSender, args: List<String>) -> Boolean)? = null,
    public var tabComplete: (suspend (sender: CommandSender, args: List<String>) -> List<String>)? = null,
    public var description: String = "",
    public var usageMessage: String = "/<command>",
    public var aliases: List<String> = emptyList(),
    public var permission: String? = null,
) {

    public fun execute(block: suspend (sender: CommandSender, args: List<String>) -> Boolean) {
        this.execute = block
    }

    public fun tabComplete(block: suspend (sender: CommandSender, args: List<String>) -> List<String>) {
        this.tabComplete = block
    }
}
