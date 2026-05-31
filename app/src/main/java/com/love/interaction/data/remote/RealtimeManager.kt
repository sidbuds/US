package com.love.interaction.data.remote

import android.util.Log
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Data refresh coordinator.
 * 
 * Strategy: polling + immediate local notifications.
 * - Polling: every 15 seconds, all ViewModels refresh from PocketBase
 * - Local: after create/delete, immediately notify all ViewModels
 * - Lifecycle: MainActivity triggers refresh on app resume
 */
object RealtimeManager {
    private const val TAG = "RealtimeManager"
    private const val POLL_INTERVAL_MS = 3_000L

    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val refreshEvents: SharedFlow<String> = _refreshEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val running = AtomicBoolean(false)
    private var pollJob: Job? = null

    fun connect(token: String, spaceId: String) {
        if (running.getAndSet(true)) return
        Log.d(TAG, "Starting auto-refresh (poll every ${POLL_INTERVAL_MS}ms)")

        pollJob = scope.launch {
            delay(3000) // let initial load finish
            while (running.get() && isActive) {
                Log.d(TAG, "Poll tick -> emitting refresh")
                _refreshEvents.tryEmit("poll")
                delay(POLL_INTERVAL_MS)
            }
        }
    }

    /** Call from MainActivity lifecycle observer on ON_RESUME */
    fun triggerRefresh() {
        Log.d(TAG, "Lifecycle refresh triggered")
        _refreshEvents.tryEmit("lifecycle")
    }

    /** Call after any create/delete operation */
    fun notifyDataChanged() {
        Log.d(TAG, "Data change notified")
        _refreshEvents.tryEmit("local_change")
    }

    fun disconnect() {
        running.set(false)
        pollJob?.cancel()
        pollJob = null
    }
}