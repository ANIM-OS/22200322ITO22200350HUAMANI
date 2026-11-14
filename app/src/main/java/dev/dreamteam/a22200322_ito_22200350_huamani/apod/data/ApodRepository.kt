package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data

import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodDao
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.toEntityOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate

data class ApodDateRange(val start: LocalDate, val end: LocalDate) {
    companion object {
        fun lastSevenDays(today: LocalDate) = ApodDateRange(today.minusDays(6), today)
    }
}

data class ApodSyncResult(val saved: Int, val skipped: Int)
class MissingNasaKeyException : IllegalStateException()
class IncompleteApodResponseException : IllegalStateException()

class ApodRepository(
    private val api: ApodApi,
    private val dao: ApodDao,
    private val apiKey: String,
    private val clock: Clock = Clock.systemDefaultZone(),
) {
    fun currentRange() = ApodDateRange.lastSevenDays(LocalDate.now(clock))

    // La UI recibe exclusivamente Flows de Room, nunca la respuesta de Retrofit.
    fun observeImages(range: ApodDateRange): Flow<List<ApodEntity>> =
        dao.observeRange(range.start.toString(), range.end.toString())

    fun observeFavorites(): Flow<List<ApodEntity>> = dao.observeFavorites()
    fun observeDetail(date: String): Flow<ApodEntity?> = dao.observeByDate(date)
    suspend fun toggleFavorite(date: String) = dao.toggleFavorite(date)

    suspend fun synchronize(range: ApodDateRange): ApodSyncResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty() || key.equals("DEMO_KEY", ignoreCase = true)) {
            throw MissingNasaKeyException()
        }
        val response = api.getApods(key, range.start.toString(), range.end.toString())
            ?: throw IncompleteApodResponseException()
        val entities = response.mapNotNull { it?.toEntityOrNull() }
            .filter { it.date >= range.start.toString() && it.date <= range.end.toString() }
            .distinctBy { it.date }
        // No borrar la caché ni favoritos antiguos si la API está vacía o incompleta.
        dao.mergeFromApi(entities)
        ApodSyncResult(saved = entities.size, skipped = response.size - entities.size)
    }
}
