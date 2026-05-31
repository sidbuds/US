package com.love.interaction.data.remote

import android.util.Log
import com.love.interaction.util.AppConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

/**
 * PocketBase realtime SSE client.
 * Connects to /api/realtime and subscribes to collection changes.
 * When data changes on the server, emits a refresh event that ViewModels collect.
 */
object RealtimeManager {
    private const val TAG = "RealtimeManager"

    private val _refreshEvents = MutableSharedFlow<String>(extraBufferCapacity = 10)
    val refreshEvents: SharedFlow<String> = _refreshEvents.asSharedFlow()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val connected = AtomicBoolean(false)
    private var thread: Thread? = null
    private var clientId: String? = null

    private val collections = listOf(
        "checkins", "interactions", "diaries", "diary_comments",
        "expenses", "wishlist", "countdowns", "couple_spaces"
    )

    fun connect(token: String, spaceId: String) {
        if (connected.getAndSet(true)) return
        Log.d(TAG, "Connecting to PocketBase realtime for spaceId=$spaceId")

        scope.launch {
            while (connected.get()) {
                try {
                    connectAndListen(token, spaceId)
                } catch (e: InterruptedException) {
                    Log.d(TAG, "Connection interrupted")
                    break
                } catch (e: Exception) {
                    Log.e(TAG, "SSE connection error: ${e.message}")
                }
                if (connected.get()) {
                    Log.d(TAG, "Reconnecting in 5s...")
                    Thread.sleep(5000)
                }
            }
        }
    }

    private fun connectAndListen(token: String, spaceId: String) {
        val client = OkHttpClient.Builder()
            .readTimeout(0, TimeUnit.MILLISECONDS) // infinite for SSE
            .connectTimeout(30, TimeUnit.SECONDS)
            .build()

        val baseUrl = AppConfig.POCKETBASE_URL.trimEnd('/')
        val request = Request.Builder()
            .url("$baseUrl/api/realtime")
            .header("Authorization", "Bearer $token")
            .build()

        Log.d(TAG, "Opening SSE connection to $baseUrl/api/realtime")

        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                Log.e(TAG, "SSE connection failed: ${response.code}")
                return
            }

            Log.d(TAG, "SSE connected, reading stream...")
            val reader = BufferedReader(InputStreamReader(response.body!!.byteStream()))
            var currentEvent = ""
            var currentData = ""

            while (connected.get()) {
                val line = reader.readLine() ?: break
                when {
                    line.startsWith("id:") -> { /* ignore */ }
                    line.startsWith("event:") -> { currentEvent = line.substringAfter("event:").trim() }
                    line.startsWith("data:") -> { currentData = line.substringAfter("data:").trim() }
                    line.isBlank() -> {
                        // End of SSE message 鈥?process it
                        if (currentData.isNotEmpty()) {
                            handleEvent(currentEvent, currentData, token, spaceId)
                        }
                        currentEvent = ""
                        currentData = ""
                    }
                }
            }
        }
    }

    private fun handleEvent(event: String, data: String, token: String, spaceId: String) {
        Log.d(TAG, "SSE event: $event, data: ${data.take(200)}")

        // PocketBase sends "PB_CONNECT" with the client ID
        if (event == "PB_CONNECT" || data.contains("\"clientId\"")) {
            try {
                val regex = "\"clientId\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val match = regex.find(data)
                if (match != null) {
                    clientId = match.groupValues[1]
                    Log.d(TAG, "Got clientId: $clientId")
                    // Subscribe to all relevant collections
                    subscribeToCollections(token)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to parse clientId: ${e.message}")
            }
            return
        }

        // Regular data change event
        try {
            val action = extractJsonString(data, "action")
            val record = data // full JSON
            Log.d(TAG, "Data change: action=$action")

            // Emit refresh event 鈥?ViewModels will react
            val result = _refreshEvents.tryEmit("data_changed")
            Log.d(TAG, "Emitted refresh event, success=$result")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to handle event: ${e.message}")
        }
    }

    private fun subscribeToCollections(token: String) {
        val cid = clientId ?: return
        val baseUrl = AppConfig.POCKETBASE_URL.trimEnd('/')

        scope.launch {
            try {
                val client = OkHttpClient.Builder()
                    .connectTimeout(10, TimeUnit.SECONDS)
                    .readTimeout(10, TimeUnit.SECONDS)
                    .build()

                for (name in collections) {
                    val body = """{"subscriptions":["$name"]}"""
                    val request = Request.Builder()
                        .url("$baseUrl/api/realtime/$cid")
                        .post(okhttp3.RequestBody.create(
                            "application/json".toMediaTypeOrNull(), body
                        ))
                        .header("Authorization", "Bearer $token")
                        .build()

                    try {
                        val resp = client.newCall(request).execute()
                        Log.d(TAG, "Subscribe '$name': ${resp.code}")
                        resp.close()
                    } catch (e: Exception) {
                        Log.e(TAG, "Subscribe '$name' failed: ${e.message}")
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Subscribe failed: ${e.message}")
            }
        }
    }

    private fun extractJsonString(json: String, key: String): String {
        val regex = "\"$key\"\\s*:\\s*\"([^\"]*)\"".toRegex()
        return regex.find(json)?.groupValues?.get(1) ?: ""
    }

    fun disconnect() {
        connected.set(false)
        thread?.interrupt()
        thread = null
        Log.d(TAG, "Disconnected")
    }
}