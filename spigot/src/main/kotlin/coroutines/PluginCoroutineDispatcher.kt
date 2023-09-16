package com.atombuilt.atomkt.spigot.coroutines

import com.atombuilt.atomkt.spigot.KotlinPlugin
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Runnable
import org.bukkit.Server
import kotlin.coroutines.CoroutineContext

internal class PluginCoroutineDispatcher(
    private val heartbeatController: ServerHeartbeatController,
    private val plugin: KotlinPlugin
) : CoroutineDispatcher(), Delay {

    private val server: Server = plugin.server

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        heartbeatController.restoreHeartbeatIfBlocked()
        return !server.isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) return
        server.scheduler.runTask(plugin, block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        heartbeatController.restoreHeartbeatIfBlocked()
        val runnable = Runnable { continuation.apply { resumeUndispatched(Unit) } }
        val delayInTicks = timeMillis / 50
        val task = server.scheduler.runTaskLater(plugin, runnable, delayInTicks)
        continuation.invokeOnCancellation { task.cancel() }
    }
}
