package com.love.interaction.data.remote

import android.util.Log
import com.love.interaction.util.AppConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.sse.EventSource
import okhttp3.sse.EventSourceListener
import okhttp3.sse.EventSources
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * PocketBase realtime client.
 * Protocol (from JS SDK source):
 * 1. GET /api/realtime -> SSE connection
 * 2. Server sends PB_CONNECT event -> lastEventId = clientId
 * 3. POST /api/realtime { clientId, subscriptions: [...] }
 * 4. Server pushes events with event name = collection name
 */
object RealtimeManager {
    private const val TAG = "RealtimeManager"

    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val refreshEvents: SharedFlow<String> = _refreshEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connected = AtomicBoolean(false)
    private var eventSource: EventSource? = null
    private var clientId: String = ""
    private var authToken: String = ""

    private val collections = listOf(
        "checkins", "interactions", "diaries", "diary_comments",
        "expenses", "wishlist", "countdowns", "couple_spaces"
    )

    fun connect(token: String, spaceId: String) {
        if (connected.getAndSet(true)) return
        authToken = token
        Log.d(TAG, "Connecting to PocketBase realtime...")
        scope.launch { connectSse() }
    }

    private fun connectSse() {
        try {
            val baseUrl = AppConfig.POCKETBASE_URL.trimEnd('/')
            val client = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(0, TimeUnit.SECONDS) // infinite for SSE
                .build()

            val request = Request.Builder()
                .url("$baseUrl/api/realtime")
                .header("Authorization", "Bearer $authToken")
                .build()

            Log.d(TAG, "Opening SSE to $baseUrl/api/realtime")

            val factory = EventSources.createFactory(client)
            eventSource = factory.newEventSource(request, object : EventSourceListener() {
                override fun onOpen(eventSource: EventSource, response: Response) {
                    Log.d(TAG, "SSE connection opened")
                }

                override fun onEvent(eventSource: EventSource, id: String?, type: String?, data: String) {
                    Log.d(TAG, "SSE event: type=$type, id=$id, data=${data.take(150)}")

                    if (type == "PB_CONNECT") {
                        // clientId comes from the lastEventId (the 'id' parameter)
                        clientId = id ?: ""
                        Log.d(TAG, "PB_CONNECT: clientId=$clientId")
                        if (clientId.isNotEmpty()) {
                            subscribeToCollections()
                        }
                        return
                    }

                    // Data change event from a collection
                    if (type != null && type in collections) {
                        Log.d(TAG, "Data change in '$type', emitting refresh")
                        _refreshEvents.tryEmit("realtime:$type")
                    }
                }

                override fun onClosed(eventSource: EventSource) {
                    Log.w(TAG, "SSE connection closed")
                    connected.set(false)
                    // Reconnect after delay
                    scope.launch {
                        delay(5000)
                        if (!connected.get()) {
                            connected.set(true)
                            connectSse()
                        }
                    }
                }

                override fun onFailure(eventSource: EventSource, t: Throwable?, response: Response?) {
                    Log.e(TAG, "SSE failure: ${t?.message}, code=${response?.code}")
                    connected.set(false)
                    eventSource.cancel()
                    // Reconnect after delay
                    scope.launch {
                        delay(5000)
                        if (!connected.get()) {
                            connected.set(true)
                            connectSse()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e(TAG, "connectSse exception: ${e.message}")
            connected.set(false)
            scope.launch {
                delay(5000)
                if (!connected.get()) {
                    connected.set(true)
                    connectSse()
                }
            }
        }
    }

    private fun subscribeToCollections() {
        if (clientId.isEmpty()) return
        Log.d(TAG, "Subscribing to collections: $collections")

        scope.launch {
            try {
                val baseUrl = AppConfig.POCKETBASE_URL.trimEnd('/')
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .build()

                // PocketBase protocol: POST /api/realtime with { clientId, subscriptions }
                val bodyJson = """{"clientId":"$clientId","subscriptions":${collections.map { "\"$it\"" }.let { "[" + it.joinToString(",") + "]" }}}"""
                Log.d(TAG, "Subscribe body: $bodyJson")

                val body = bodyJson.toRequestBody("application/json".toMediaTypeOrNull())
                val request = Request.Builder()
                    .url("$baseUrl/api/realtime")
                    .post(body)
                    .header("Authorization", "Bearer $authToken")
                    .build()

                val response = client.newCall(request).execute()
                Log.d(TAG, "Subscribe response: ${response.code}")
                response.close()
            } catch (e: Exception) {
                Log.e(TAG, "Subscribe failed: ${e.message}")
            }
        }
    }

    fun notifyDataChanged() {
        Log.d(TAG, "Local data change notified")
        _refreshEvents.tryEmit("local_change")
    }

    fun disconnect() {
        connected.set(false)
        eventSource?.cancel()
        eventSource = null
        clientId = ""
        Log.d(TAG, "Disconnected")
    }
}