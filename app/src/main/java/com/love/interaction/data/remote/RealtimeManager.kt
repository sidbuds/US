package com.love.interaction.data.remote

import android.util.Log
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow

/**
 * Simple event bus for data change notifications.
 * ViewModels use startAutoRefresh() with their own coroutine scope instead.
 */
object RealtimeManager {
    private const val TAG = "RealtimeManager"

    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val refreshEvents: SharedFlow<String> = _refreshEvents.asSharedFlow()

    /** Call after any create/delete to notify other ViewModels */
    fun notifyDataChanged() {
        Log.d(TAG, "Data change notified")
        _refreshEvents.tryEmit("local_change")
    }

    /** Call from lifecycle observer on app resume */
    fun triggerRefresh() {
        Log.d(TAG, "Lifecycle refresh triggered")
        _refreshEvents.tryEmit("lifecycle")
    }
}