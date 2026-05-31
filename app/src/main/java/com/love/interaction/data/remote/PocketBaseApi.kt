package com.love.interaction.data.remote

import com.love.interaction.data.model.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * PocketBase REST API interface.
 * All endpoints follow PocketBase's standard CRUD pattern:
 *   GET    /api/collections/{collection}/records
 *   POST   /api/collections/{collection}/records
 *   GET    /api/collections/{collection}/records/{id}
 *   PATCH  /api/collections/{collection}/records/{id}
 *   DELETE /api/collections/{collection}/records/{id}
 */
interface PocketBaseApi {

    // ==================== Auth ====================

    @POST("api/collections/users/auth-with-password")
    suspend fun authWithPassword(
        @Body body: Map<String, String>
    ): Response<AuthResponse>

    @POST("api/collections/users/records")
    suspend fun registerUser(
        @Body body: Map<String, String>
    ): Response<User>

    @POST("api/collections/users/request-password-reset")
    suspend fun requestPasswordReset(
        @Body body: Map<String, String>
    ): Response<Unit>

    // ==================== Couple Spaces ====================

    @GET("api/collections/couple_spaces/records")
    suspend fun getCoupleSpaces(
        @Query("filter") filter: String,
        @Query("perPage") perPage: Int = 1
    ): Response<PbListResponse<CoupleSpace>>

    @POST("api/collections/couple_spaces/records")
    suspend fun createCoupleSpace(
        @Body body: CoupleSpaceCreateRequest
    ): Response<CoupleSpace>

    @PATCH("api/collections/couple_spaces/records/{id}")
    suspend fun joinCoupleSpace(
        @Path("id") id: String,
        @Body body: CoupleSpaceJoinRequest
    ): Response<CoupleSpace>

    // ==================== Checkins ====================

    @GET("api/collections/checkins/records")
    suspend fun getCheckins(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<Checkin>>

    @POST("api/collections/checkins/records")
    suspend fun createCheckin(
        @Body body: CheckinCreateRequest
    ): Response<Checkin>

    // ==================== Interactions ====================

    @GET("api/collections/interactions/records")
    suspend fun getInteractions(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<Interaction>>

    @POST("api/collections/interactions/records")
    suspend fun createInteraction(
        @Body body: InteractionCreateRequest
    ): Response<Interaction>

    @PATCH("api/collections/interactions/records/{id}")
    suspend fun markInteractionRead(
        @Path("id") id: String,
        @Body body: Map<String, Boolean>
    ): Response<Interaction>

    // ==================== Diaries ====================

    @GET("api/collections/diaries/records")
    suspend fun getDiaries(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 20
    ): Response<PbListResponse<Diary>>

    @GET("api/collections/diaries/records/{id}")
    suspend fun getDiaryById(@Path("id") id: String): Response<Diary>

    @POST("api/collections/diaries/records")
    suspend fun createDiary(@Body body: DiaryCreateRequest): Response<Diary>

    @Multipart
    @POST("api/collections/diaries/records")
    suspend fun createDiaryWithFiles(
        @Part("space_id") spaceId: okhttp3.RequestBody,
        @Part("user_id") userId: okhttp3.RequestBody,
        @Part("category") category: okhttp3.RequestBody,
        @Part("title") title: okhttp3.RequestBody,
        @Part("content") content: okhttp3.RequestBody,
        @Part images: List<MultipartBody.Part>
    ): Response<Diary>

    @Multipart
    @PATCH("api/collections/diaries/records/{id}")
    suspend fun addDiaryFiles(
        @Path("id") id: String,
        @Part images: List<MultipartBody.Part>
    ): Response<Diary>

    @PATCH("api/collections/diaries/records/{id}")
    suspend fun updateDiary(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<Diary>

    @DELETE("api/collections/diaries/records/{id}")
    suspend fun deleteDiary(@Path("id") id: String): Response<Unit>

    // ==================== Diary Likes ====================

    @GET("api/collections/diary_likes/records")
    suspend fun getDiaryLikes(
        @Query("filter") filter: String
    ): Response<PbListResponse<DiaryLike>>

    @POST("api/collections/diary_likes/records")
    suspend fun createDiaryLike(
        @Body body: Map<String, String>
    ): Response<DiaryLike>

    @DELETE("api/collections/diary_likes/records/{id}")
    suspend fun deleteDiaryLike(@Path("id") id: String): Response<Unit>

    // ==================== Diary Comments ====================

    @GET("api/collections/diary_comments/records")
    suspend fun getDiaryComments(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "created",
        @Query("perPage") perPage: Int = 100
    ): Response<PbListResponse<DiaryComment>>

    @POST("api/collections/diary_comments/records")
    suspend fun createDiaryComment(
        @Body body: DiaryCommentCreateRequest
    ): Response<DiaryComment>

    @DELETE("api/collections/diary_comments/records/{id}")
    suspend fun deleteDiaryComment(@Path("id") id: String): Response<Unit>

    // ==================== Coins ====================

    @GET("api/collections/coins/records")
    suspend fun getCoinBalance(
        @Query("filter") filter: String
    ): Response<PbListResponse<CoinBalance>>

    @POST("api/collections/coins/records")
    suspend fun createCoinBalance(
        @Body body: Map<String, Any>
    ): Response<CoinBalance>

    @PATCH("api/collections/coins/records/{id}")
    suspend fun updateCoinBalance(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<CoinBalance>

    @GET("api/collections/coin_transactions/records")
    suspend fun getCoinTransactions(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<CoinTransaction>>

    @POST("api/collections/coin_transactions/records")
    suspend fun createCoinTransaction(
        @Body body: CoinTransactionCreateRequest
    ): Response<CoinTransaction>

    // ==================== Expenses ====================

    @GET("api/collections/expenses/records")
    suspend fun getExpenses(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-date",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<Expense>>

    @POST("api/collections/expenses/records")
    suspend fun createExpense(
        @Body body: ExpenseCreateRequest
    ): Response<Expense>

    @PATCH("api/collections/expenses/records/{id}")
    suspend fun updateExpense(
        @Path("id") id: String,
        @Body body: Map<String, Any>
    ): Response<Expense>

    @DELETE("api/collections/expenses/records/{id}")
    suspend fun deleteExpense(@Path("id") id: String): Response<Unit>

    // ==================== Wishlist ====================

    @GET("api/collections/wishlist/records")
    suspend fun getWishlists(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created",
        @Query("page") page: Int = 1,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<Wishlist>>

    @POST("api/collections/wishlist/records")
    suspend fun createWishlist(
        @Body body: WishlistCreateRequest
    ): Response<Wishlist>

    @PATCH("api/collections/wishlist/records/{id}")
    suspend fun updateWishlist(
        @Path("id") id: String,
        @Body body: okhttp3.RequestBody
    ): Response<Wishlist>

    @Multipart
    @PATCH("api/collections/wishlist/records/{id}")
    suspend fun addWishlistImages(
        @Path("id") id: String,
        @Part images: List<MultipartBody.Part>
    ): Response<Wishlist>

    @DELETE("api/collections/wishlist/records/{id}")
    suspend fun deleteWishlist(@Path("id") id: String): Response<Unit>

    @GET("api/collections/wishlist_contributions/records")
    suspend fun getWishlistContributions(
        @Query("filter") filter: String,
        @Query("sort") sort: String = "-created"
    ): Response<PbListResponse<WishlistContribution>>

    @POST("api/collections/wishlist_contributions/records")
    suspend fun createWishlistContribution(
        @Body body: WishlistContributionCreateRequest
    ): Response<WishlistContribution>

    // ==================== File Upload ====================

    @Multipart
    @POST("api/collections/{collection}/records/{id}")
    suspend fun uploadFile(
        @Path("collection") collection: String,
        @Path("id") recordId: String,
        @Part file: MultipartBody.Part
    ): Response<Any>

    // ==================== Countdowns ====================

    @GET("api/collections/countdowns/records")
    suspend fun getCountdowns(
        @Query("filter") filter: String,
        @Query("perPage") perPage: Int = 50
    ): Response<PbListResponse<Countdown>>

    @POST("api/collections/countdowns/records")
    suspend fun createCountdown(@Body body: CountdownCreateRequest): Response<Countdown>

    @PATCH("api/collections/countdowns/records/{id}")
    suspend fun updateCountdown(
        @Path("id") id: String,
        @Body body: okhttp3.RequestBody
    ): Response<Countdown>

    @DELETE("api/collections/countdowns/records/{id}")
    suspend fun deleteCountdown(@Path("id") id: String): Response<Unit>
}



