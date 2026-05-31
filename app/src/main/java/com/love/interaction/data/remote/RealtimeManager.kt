package com.love.interaction.data.remote

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Manages data refresh: periodic polling + local events.
 * When data changes (either from PocketBase or local actions), emits refresh events
 * that ViewModels collect to reload their data.
 */
object RealtimeManager {
    private const val TAG = "RealtimeManager"
    private const val POLL_INTERVAL_MS = 15_000L // 15 seconds

    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val refreshEvents: SharedFlow<String> = _refreshEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    private var pollJob: Job? = null

    /**
     * Start polling for changes. Call after login.
     */
    fun connect(token: String, spaceId: String) {
        if (running.getAndSet(true)) return
        Log.d(TAG, "Starting auto-refresh polling (interval=${POLL_INTERVAL_MS}ms)")

        pollJob = scope.launch {
            // Small delay to let initial load complete
            delay(5000)
            while (running.get() && isActive) {
                try {
                    Log.d(TAG, "Polling: emitting refresh event")
                    _refreshEvents.tryEmit("poll")
                } catch (e: Exception) {
                    Log.e(TAG, "Poll error: ${e.message}")
                }
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    /**
     * Trigger an immediate refresh. Call after local create/delete operations
     * to notify other ViewModels to reload.
     */
    fun notifyDataChanged() {
        Log.d(TAG, "Local data change notified")
        _refreshEvents.tryEmit("local_change")
    }

    fun disconnect() {
        running.set(false)
        pollJob?.cancel()
        pollJob = null
        Log.d(TAG, "Stopped")
    }
}