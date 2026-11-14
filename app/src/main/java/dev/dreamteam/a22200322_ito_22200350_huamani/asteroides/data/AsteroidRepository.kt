package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data

import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidDao
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.remote.AsteroidApiService
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.remote.toEntity
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

class AsteroidRepository(
    private val api: AsteroidApiService,
    private val dao: AsteroidDao,
    private val apiKey: String,
) {
    fun observeRecent(startDate: LocalDate, endDate: LocalDate): Flow<List<AsteroidEntity>> =
        dao.observeRecent(startDate.toString(), endDate.toString())

    fun observeFavorites(): Flow<List<AsteroidEntity>> = dao.observeFavorites()

    fun observeById(id: String): Flow<AsteroidEntity?> = dao.observeById(id)

    suspend fun toggleFavorite(id: String) = dao.toggleFavorite(id)

    /** Fetches the seven calendar days ending today; the interface remains backed by Room. */
    suspend fun syncRecent(endDate: LocalDate = LocalDate.now()) {
        require(apiKey.isNotBlank()) { "Configura NASA_API_KEY antes de sincronizar asteroides." }
        val startDate = endDate.minusDays(6)
        try {
            val response = api.getFeed(apiKey, startDate.toString(), endDate.toString())
            if (!response.isSuccessful) {
                throw AsteroidSyncException("NASA respondió con HTTP ${response.code()}.")
            }
            val groups = response.body()?.nearEarthObjects
                ?: throw AsteroidSyncException("La respuesta de NASA no incluye asteroides.")
            val items = groups.flatMap { (date, asteroids) ->
                asteroids.orEmpty().mapNotNull { it.toEntity(date) }
            }
            if (groups.values.any { !it.isNullOrEmpty() } && items.isEmpty()) {
                throw AsteroidSyncException("NASA devolvió datos de asteroides incompletos.")
            }
            // A NASA ID can occur more than once in a feed. Keep its latest approach.
            val unique = items.groupBy { it.id }.values.map { records ->
                records.maxBy { it.approachDate }
            }
            dao.saveFeedPreservingFavorites(unique, startDate.toString(), endDate.toString())
        } catch (error: CancellationException) {
            throw error
        } catch (error: AsteroidSyncException) {
            throw error
        } catch (error: Exception) {
            throw AsteroidSyncException("No se pudo actualizar Asteroides. Comprueba la conexión e inténtalo de nuevo.", error)
        }
    }
}

class AsteroidSyncException(message: String, cause: Throwable? = null) : Exception(message, cause)
