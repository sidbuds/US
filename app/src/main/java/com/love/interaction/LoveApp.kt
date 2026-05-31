package com.love.interaction

import android.app.Application
import android.util.Log
import com.love.interaction.data.local.AppDatabase
import com.love.interaction.data.remote.PocketBaseClient
import com.love.interaction.data.repository.SessionManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class LoveApp : Application() {
    companion object {
        lateinit var instance: LoveApp
            private set
        private const val TAG = "LoveApp"
    }

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        instance = this
        Log.d(TAG, "onCreate START")
        appScope.launch {
            try {
                val db = AppDatabase.getInstance(this@LoveApp)
                val sm = SessionManager(db.sessionDao())
                val session = sm.getSession()
                Log.d(TAG, "onCreate: session=${session != null}, email='${session?.email}', token=${session?.token?.take(20)}..., spaceId='${session?.spaceId}'")
                if (!session?.token.isNullOrEmpty()) {
                    PocketBaseClient.setAuthToken(session!!.token)
                    Log.d(TAG, "onCreate: Token restored for ${session.email}")
                } else {
                    Log.d(TAG, "onCreate: No valid session found")
                }
            } catch (e: Exception) {
                Log.e(TAG, "onCreate: EXCEPTION", e)
            }
        }
    }
}
