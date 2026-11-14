package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

interface ApodApi {
    @GET("planetary/apod")
    suspend fun getApods(
        @Query("api_key") apiKey: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
    ): List<ApodDto?>?
}
