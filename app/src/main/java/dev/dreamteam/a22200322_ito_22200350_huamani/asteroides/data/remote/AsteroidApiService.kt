package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface AsteroidApiService {
    @GET("neo/rest/v1/feed")
    suspend fun getFeed(
        @Query("api_key") apiKey: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): Response<AsteroidFeedDto>
}
