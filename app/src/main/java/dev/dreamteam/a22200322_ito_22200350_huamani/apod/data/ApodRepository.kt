package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data

import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodDao
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodArchiveApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.isNasaPlaceholder
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.toEntityOrNull
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.CancellationException
import retrofit2.HttpException
import java.time.format.DateTimeFormatter
import kotlinx.coroutines.withContext
import java.time.Clock
import java.time.LocalDate
import java.time.temporal.ChronoUnit

data class ApodDateRange(val start: LocalDate, val end: LocalDate) {
    companion object {
        fun lastSevenDays(today: LocalDate) = ApodDateRange(today.minusDays(6), today)

        fun archive(start: String, end: String, today: LocalDate): ApodDateRange {
            val from = try { LocalDate.parse(start.trim()) } catch (_: Exception) {
                throw IllegalArgumentException("Escribe la fecha inicial como AAAA-MM-DD.")
            }
            val to = try { LocalDate.parse(end.trim()) } catch (_: Exception) {
                throw IllegalArgumentException("Escribe la fecha final como AAAA-MM-DD.")
            }
            require(!from.isBefore(LocalDate.of(1995, 6, 16))) { "El archivo APOD empieza el 16 de junio de 1995." }
            require(!to.isBefore(from)) { "La fecha final debe ser igual o posterior a la inicial." }
            require(!to.isAfter(today)) { "Elige fechas hasta hoy; no hay imágenes futuras." }
            require(ChronoUnit.DAYS.between(from, to) < 31) { "Elige hasta 31 días por consulta." }
            return ApodDateRange(from, to)
        }
    }
}

data class ApodSyncResult(val saved: Int, val skipped: Int, val usedOfficialArchive: Boolean = false)
class MissingNasaKeyException : IllegalStateException()
class IncompleteApodResponseException : IllegalStateException()

class ApodRepository(
    private val api: ApodApi,
    private val dao: ApodDao,
    private val apiKey: String,
    private val clock: Clock = Clock.systemDefaultZone(),
    private val archive: ApodArchiveApi? = null,
) {
    fun currentRange() = ApodDateRange.lastSevenDays(LocalDate.now(clock))

    // La UI recibe exclusivamente Flows de Room, nunca la respuesta de Retrofit.
    fun observeImages(range: ApodDateRange): Flow<List<ApodEntity>> =
        dao.observeRange(range.start.toString(), range.end.toString()).map { rows -> rows.filterNot { it.isNasaPlaceholder() } }

    fun observeFavorites(): Flow<List<ApodEntity>> = dao.observeFavorites().map { rows -> rows.filterNot { it.isNasaPlaceholder() } }
    fun observeDetail(date: String): Flow<ApodEntity?> = dao.observeByDate(date).map { it?.takeUnless { row -> row.isNasaPlaceholder() } }
    suspend fun toggleFavorite(date: String) = dao.toggleFavorite(date)

    suspend fun synchronize(range: ApodDateRange): ApodSyncResult = withContext(Dispatchers.IO) {
        val key = apiKey.trim()
        if (key.isEmpty() || key.equals("DEMO_KEY", ignoreCase = true)) {
            throw MissingNasaKeyException()
        }
        var usedArchive = false
        val primary = try {
            api.getApods(key, range.start.toString(), range.end.toString())
        } catch (error: CancellationException) {
            throw error
        } catch (error: Exception) {
            // No eludir rechazos de clave ni cuotas del gateway.
            if (archive == null || (error is HttpException && error.code() in listOf(400, 401, 403, 429))) throw error
            null
        }
        val containsPlaceholder = primary.orEmpty().any { dto ->
            listOfNotNull(dto?.url, dto?.hdUrl).any { it.contains("nasa-logo", ignoreCase = true) }
        }
        val response = if (archive != null && (primary == null || containsPlaceholder)) {
            usedArchive = true
            val format = DateTimeFormatter.ofPattern("yyMMdd")
            archive.getRange(range.start.format(format), range.end.format(format))
        } else primary
        if (response == null) throw IncompleteApodResponseException()
        val entities = response.mapNotNull { it?.toEntityOrNull() }
            .filter { it.date >= range.start.toString() && it.date <= range.end.toString() }
            .distinctBy { it.date }
        // No borrar la caché ni favoritos antiguos si la API está vacía o incompleta.
        dao.mergeFromApi(entities)
        ApodSyncResult(saved = entities.size, skipped = response.size - entities.size, usedOfficialArchive = usedArchive)
    }
}
