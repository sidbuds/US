package com.love.interaction.data.repository

import com.love.interaction.data.local.CachedCheckin
import com.love.interaction.data.local.dao.CheckinDao
import com.love.interaction.data.model.Checkin
import com.love.interaction.data.model.CheckinCreateRequest
import com.love.interaction.data.model.CheckinType
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow
import java.time.Instant

class CheckinRepository(
    private val checkinDao: CheckinDao
) {
    private val api = PocketBaseClient.api

    fun getCheckins(spaceId: String): Flow<List<CachedCheckin>> {
        return checkinDao.getCheckinsBySpace(spaceId)
    }

    suspend fun refreshCheckins(spaceId: String): Result<List<Checkin>> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getCheckins(filter)
            if (response.isSuccessful) {
                val checkins = response.body()?.items ?: emptyList()
                // Cache locally
                checkinDao.clearForSpace(spaceId)
                checkinDao.insertAll(checkins.map { it.toCached() })
                Result.success(checkins)
            } else {
                Result.failure(Exception("加载报备失败 (${response.code()})"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun createCheckin(
        spaceId: String,
        userId: String,
        type: CheckinType,
        customContent: String = "",
        locationLat: Double? = null,
        locationLng: Double? = null,
        locationName: String = ""
    ): Result<Checkin> {
        return try {
            val request = CheckinCreateRequest(
                spaceId = spaceId,
                userId = userId,
                type = type.key,
                customContent = customContent,
                locationLat = locationLat,
                locationLng = locationLng,
                locationName = locationName
            )
            val response = api.createCheckin(request)
            if (response.isSuccessful) {
                val checkin = response.body()!!
                checkinDao.insert(checkin.toCached())
                Result.success(checkin)
            } else {
                Result.failure(Exception("报备失败 (${response.code()}): ${response.errorBody()?.string() ?: ""}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Checkin.toCached() = CachedCheckin(
        id = id,
        spaceId = spaceId,
        userId = userId,
        type = type,
        customContent = customContent,
        locationLat = locationLat,
        locationLng = locationLng,
        locationName = locationName,
        createdAt = createdAt
    )
}
