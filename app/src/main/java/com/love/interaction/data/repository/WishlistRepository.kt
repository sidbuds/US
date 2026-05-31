package com.love.interaction.data.repository

import android.content.Context
import android.net.Uri
import com.love.interaction.data.local.CachedWishlist
import com.love.interaction.data.local.dao.WishlistDao
import com.love.interaction.data.model.Wishlist
import com.love.interaction.data.model.WishlistCreateRequest
import com.love.interaction.data.remote.PocketBaseClient
import kotlinx.coroutines.flow.Flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

class WishlistRepository(private val wishlistDao: WishlistDao, private val context: Context) {
    private val api = PocketBaseClient.api
    private val JSON_MEDIA = "application/json; charset=utf-8".toMediaType()

    fun getWishlists(spaceId: String): Flow<List<CachedWishlist>> = wishlistDao.getWishlistsBySpace(spaceId)

    suspend fun refreshWishlists(spaceId: String): Result<List<Wishlist>> {
        return try {
            val filter = "space_id=\"$spaceId\""
            val response = api.getWishlists(filter)
            if (response.isSuccessful) {
                val wishlists = response.body()?.items ?: emptyList()
                wishlistDao.clearForSpace(spaceId)
                wishlistDao.insertAll(wishlists.map { it.toCached() })
                Result.success(wishlists)
            } else Result.failure(Exception("加载愿望清单失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun createWishlist(spaceId: String, userId: String, name: String): Result<Wishlist> {
        return try {
            val request = WishlistCreateRequest(spaceId = spaceId, userId = userId, name = name)
            val response = api.createWishlist(request)
            if (response.isSuccessful) {
                val w = response.body()!!
                wishlistDao.insert(w.toCached())
                Result.success(w)
            } else Result.failure(Exception("创建愿望失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun fulfillWish(wishId: String, description: String, imageUris: List<Uri>): Result<Wishlist> {
        return try {
            // Update status + description
            val json = """{"status":"completed","fulfill_description":"${description.replace("\"", "\\\"")}"}"""
            val body = json.toRequestBody(JSON_MEDIA)
            val response = api.updateWishlist(wishId, body)
            if (response.isSuccessful) {
                var wish = response.body()!!
                // Upload images if any
                if (imageUris.isNotEmpty()) {
                    val parts = imageUris.mapIndexed { idx, uri ->
                        val file = uriToFile(uri, "wish_$idx")
                        val reqBody = file.asRequestBody("image/*".toMediaType())
                        MultipartBody.Part.createFormData("fulfill_images", file.name, reqBody)
                    }
                    val imgResp = api.addWishlistImages(wishId, parts)
                    if (imgResp.isSuccessful) wish = imgResp.body()!!
                }
                wishlistDao.insert(wish.toCached())
                Result.success(wish)
            } else Result.failure(Exception("还愿失败: ${response.code()}"))
        } catch (e: Exception) { Result.failure(e) }
    }

    suspend fun deleteWishlist(id: String): Result<Unit> {
        return try {
            val response = api.deleteWishlist(id)
            if (response.isSuccessful) { wishlistDao.deleteById(id); Result.success(Unit) }
            else Result.failure(Exception("删除愿望失败"))
        } catch (e: Exception) { Result.failure(e) }
    }

    private fun uriToFile(uri: Uri, prefix: String): File {
        val input = context.contentResolver.openInputStream(uri)!!
        val bitmap = android.graphics.BitmapFactory.decodeStream(input)
        input.close()
        val tempFile = File(context.cacheDir, "${prefix}_${System.currentTimeMillis()}.jpg")
        val scaled = if (bitmap.width > 1024) {
            android.graphics.Bitmap.createScaledBitmap(bitmap, 1024, (bitmap.height * 1024f / bitmap.width).toInt(), true)
        } else bitmap
        tempFile.outputStream().use { scaled.compress(android.graphics.Bitmap.CompressFormat.JPEG, 80, it) }
        if (scaled !== bitmap) scaled.recycle()
        bitmap.recycle()
        return tempFile
    }

    private fun Wishlist.toCached() = CachedWishlist(
        id = id, collectionId = collectionId, spaceId = spaceId, userId = userId, name = name,
        targetCoins = targetCoins, currentBalance = currentBalance,
        imageUrl = imageUrl, status = status,
        fulfillDescription = fulfillDescription,
        fulfillImages = com.google.gson.Gson().toJson(fulfillImages),
        completedAt = completedAt, createdAt = createdAt, updatedAt = updatedAt
    )
}

