package com.atombuilt.atomkt.spigot.coroutines

import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Delay
import kotlinx.coroutines.Runnable
import org.bukkit.Server
import org.bukkit.plugin.Plugin
import kotlin.coroutines.CoroutineContext

internal class PluginAsyncCoroutineDispatcher(
    private val plugin: Plugin
) : CoroutineDispatcher(), Delay {

    private val server: Server = plugin.server

    override fun isDispatchNeeded(context: CoroutineContext): Boolean {
        return plugin.server.isPrimaryThread
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        if (!plugin.isEnabled) return
        server.scheduler.runTaskAsynchronously(plugin, block)
    }

    override fun scheduleResumeAfterDelay(timeMillis: Long, continuation: CancellableContinuation<Unit>) {
        val runnable = Runnable { continuation.apply { resumeUndispatched(Unit) } }
        val delayInTicks = timeMillis / 50
        val task = server.scheduler.runTaskLaterAsynchronously(plugin, runnable, delayInTicks)
        continuation.invokeOnCancellation { task.cancel() }
    }
}
