package com.atombuilt.atomkt.spigot.coroutines

import com.atombuilt.atomkt.commons.reflection.access
import com.atombuilt.atomkt.spigot.KotlinPlugin
import com.atombuilt.atomkt.spigot.nms.NMSVersion
import kotlinx.coroutines.*
import org.bukkit.Server
import java.util.concurrent.locks.LockSupport
import kotlin.concurrent.thread

internal class ServerHeartbeatController(
    private val plugin: KotlinPlugin
) {

    private val server: Server = plugin.server

    @Volatile
    private var isEnabled: Boolean = false

    @Volatile
    private lateinit var primaryThread: Thread

    fun runControlled(block: suspend CoroutineScope.() -> Unit) {
        isEnabled = true
        try {
            runBlocking(context = plugin.coroutineContext, block = block)
        } finally {
            isEnabled = false
        }
    }

    fun restoreHeartbeatIfBlocked() {
        if (!isEnabled) return
        if (server.isPrimaryThread) primaryThread = Thread.currentThread()
        if (!this::primaryThread.isInitialized) return
        val isHeartbeatBlocked = LockSupport.getBlocker(primaryThread) != null
        if (!isHeartbeatBlocked) return
        restoreHeartbeat()
    }

    private fun restoreHeartbeat() {
        val craftSchedulerClazz = Class.forName("org.bukkit.craftbukkit.%s.scheduler.CraftScheduler".format(NMSVersion))
        val schedulerTickField = craftSchedulerClazz.getDeclaredField("currentTick")
        val schedulerHeartBeatMethod = craftSchedulerClazz.getDeclaredMethod("mainThreadHeartbeat", Int::class.java)
        thread(start = true, name = "Server Heartbeat Restorer", priority = Thread.MAX_PRIORITY) {
            val scheduler = server.scheduler
            val currentTick = schedulerTickField.access { get(scheduler) }
            schedulerHeartBeatMethod.access { invoke(scheduler, currentTick) }
        }
    }
}
