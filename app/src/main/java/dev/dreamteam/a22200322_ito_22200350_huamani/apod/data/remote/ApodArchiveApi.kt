package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote

import retrofit2.http.GET
import retrofit2.http.Query

/** Respaldo oficial usado por el propio servidor APOD tras la migración de NASA Science.
 * No recibe ni transmite la clave del gateway api.nasa.gov.
 */
interface ApodArchiveApi {
    @GET("https://science.nasa.gov/wp-json/wp/v2/apod-basic")
    suspend fun getRange(
        @Query("date_from") startDate: String,
        @Query("date_to") endDate: String,
    ): List<ApodDto?>?
}
