package com.atombuilt.atomkt.spigot.example

import com.atombuilt.atomkt.config.ConfigManager
import com.atombuilt.atomkt.config.declareConfigByClasspath
import com.atombuilt.atomkt.spigot.KotlinPlugin
import com.atombuilt.atomkt.spigot.command.attachCommand
import com.atombuilt.atomkt.spigot.example.ExampleConfig.Companion.exampleConfig
import com.atombuilt.atomkt.spigot.listener.attachListener
import org.bukkit.event.player.PlayerJoinEvent

class ExamplePlugin : KotlinPlugin() {

    override suspend fun attachComponents() {
        attachListener<PlayerJoinEvent> { player.sendMessage(exampleConfig.welcomeMessage) }
        attachCommand("example") {
            description = "An example command."

            execute { sender, args ->
                sender.sendMessage("An example output. Entered arguments: ${args.joinToString(" ")}")
                return@execute true
            }
            tabComplete { _, args ->
                when (args.size) {
                    1 -> listOf("First", "1")
                    2 -> listOf("Second", "2")
                    3 -> listOf("Third", "3")
                    else -> emptyList()
                }
            }
        }
    }

    override suspend fun onEnabled() {
        log.info { "Example plugin has been enabled!" }
    }

    override suspend fun onDisabled() {
        log.info { "Example plugin has been disabled!" }
    }

    override fun ConfigManager.configuration() {
        declareConfigByClasspath<ExampleConfig>("config/example.yml")
    }
}
