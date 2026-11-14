package dev.dreamteam.a22200322_ito_22200350_huamani.apod

import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.*
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodDao
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodArchiveApi
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote.ApodDto
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test
import java.io.IOException
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

class ApodRepositoryTest {
    private val clock = Clock.fixed(Instant.parse("2026-11-14T12:00:00Z"), ZoneOffset.UTC)

    @Test fun sevenDayRangeCrossesMonthAndYearBoundaries() {
        val range = ApodDateRange.lastSevenDays(LocalDate.parse("2026-01-03"))
        assertEquals("2025-12-28", range.start.toString())
        assertEquals("2026-01-03", range.end.toString())
    }

    @Test fun synchronizationPassesDateParametersAndSkipsInvalidRecords() = runBlocking {
        val api = FakeApi(listOf(dto("2026-11-14"), null, dto("bad"), dto("2026-11-07"), dto("2026-11-14"), dto("2026-11-08")))
        val dao = MemoryDao()
        val repository = ApodRepository(api, dao, "test-key", clock)
        val result = repository.synchronize(repository.currentRange())
        assertEquals(listOf("test-key", "2026-11-08", "2026-11-14"), api.arguments)
        assertEquals(ApodSyncResult(2, 4), result)
        assertEquals(listOf("2026-11-14", "2026-11-08"), repository.observeImages(repository.currentRange()).first().map { it.date })
    }

    @Test fun refreshKeepsFavoriteWhileUpdatingContentAndKeepsOlderFavorites() = runBlocking {
        val api = FakeApi(listOf(dto("2026-11-14")))
        val dao = MemoryDao()
        val repository = ApodRepository(api, dao, "test-key", clock)
        dao.mergeFromApi(listOf(entity("2026-11-01")))
        dao.toggleFavorite("2026-11-01")
        repository.synchronize(repository.currentRange())
        repository.toggleFavorite("2026-11-14")
        api.response = listOf(dto("2026-11-14").copy(title = "Título actualizado"))
        repository.synchronize(repository.currentRange())
        assertEquals(listOf("2026-11-14", "2026-11-01"), repository.observeFavorites().first().map { it.date })
        assertEquals("Título actualizado", repository.observeDetail("2026-11-14").first()?.title)
        repository.toggleFavorite("2026-11-14")
        assertEquals(listOf("2026-11-01"), repository.observeFavorites().first().map { it.date })
    }

    @Test fun networkFailureDoesNotEraseStoredImagesOrFavorites() = runBlocking {
        val api = FakeApi(listOf(dto("2026-11-14")))
        val repository = ApodRepository(api, MemoryDao(), "test-key", clock)
        repository.synchronize(repository.currentRange())
        repository.toggleFavorite("2026-11-14")
        api.failure = IOException("offline")
        try {
            repository.synchronize(repository.currentRange())
            fail("Expected network failure")
        } catch (_: IOException) { }
        assertTrue(requireNotNull(repository.observeDetail("2026-11-14").first()).isFavorite)
    }

    @Test fun emptyAndNullResponsesDoNotDeleteCache() = runBlocking {
        val api = FakeApi(listOf(dto("2026-11-14")))
        val repository = ApodRepository(api, MemoryDao(), "test-key", clock)
        repository.synchronize(repository.currentRange())
        api.response = emptyList()
        assertEquals(ApodSyncResult(0, 0), repository.synchronize(repository.currentRange()))
        api.response = null
        try {
            repository.synchronize(repository.currentRange())
            fail("Expected incomplete response")
        } catch (_: IncompleteApodResponseException) { }
        assertEquals(1, repository.observeImages(repository.currentRange()).first().size)
    }

    @Test fun missingOrDemoKeyNeverCallsNasa() = runBlocking {
        for (key in listOf("", "   ", "DEMO_KEY", "demo_key")) {
            val api = FakeApi(emptyList())
            val repository = ApodRepository(api, MemoryDao(), key, clock)
            try {
                repository.synchronize(repository.currentRange())
                fail("Expected missing key")
            } catch (_: MissingNasaKeyException) { }
            assertNull(api.arguments)
        }
    }

    @Test fun cancellationIsPropagated() = runBlocking {
        val api = FakeApi(emptyList()).apply { failure = CancellationException("cancelled") }
        val repository = ApodRepository(api, MemoryDao(), "test-key", clock)
        try {
            repository.synchronize(repository.currentRange())
            fail("Expected cancellation")
        } catch (_: CancellationException) { }
    }

    @Test fun nasaLogoResponseIsRecoveredFromOfficialArchiveForTheSameDates() = runBlocking {
        val primary = FakeApi(listOf(dto("2026-11-14").copy(title = "NASA Science",
            url = "https://science.nasa.gov/wp-content/themes/nasa-child/assets/images/nasa-logo@2x.png")))
        var archiveDates: List<String>? = null
        val archive = object : ApodArchiveApi {
            override suspend fun getRange(startDate: String, endDate: String): List<ApodDto?> {
                archiveDates = listOf(startDate, endDate)
                return listOf(dto("2026-11-14").copy(title = "Apollo 12", hdUrl = "https://assets.science.nasa.gov/moon.jpg"))
            }
        }
        val repository = ApodRepository(primary, MemoryDao(), "test-key", clock, archive)
        assertEquals(ApodSyncResult(1, 0, true), repository.synchronize(repository.currentRange()))
        assertEquals(listOf("261108", "261114"), archiveDates)
        assertEquals("Apollo 12", repository.observeImages(repository.currentRange()).first().single().title)
    }

    @Test fun gatewayFailureCanRecoverWithoutErasingLocalFavorites() = runBlocking {
        val primary = FakeApi(emptyList()).apply { failure = IOException("unavailable") }
        val archive = object : ApodArchiveApi {
            override suspend fun getRange(startDate: String, endDate: String) = listOf(dto("2026-11-14"))
        }
        val dao = MemoryDao()
        dao.mergeFromApi(listOf(entity("2026-11-01")))
        dao.toggleFavorite("2026-11-01")
        val repository = ApodRepository(primary, dao, "test-key", clock, archive)
        assertTrue(repository.synchronize(repository.currentRange()).usedOfficialArchive)
        assertEquals("2026-11-01", repository.observeFavorites().first().single().date)
    }

    private fun dto(date: String) = ApodDto(date = date, title = "Imagen", mediaType = "image")
    private fun entity(date: String) = ApodEntity(date, "Imagen", "Texto", "image", null, null, null)

    private class FakeApi(var response: List<ApodDto?>?) : ApodApi {
        var arguments: List<String>? = null
        var failure: Exception? = null
        override suspend fun getApods(apiKey: String, startDate: String, endDate: String): List<ApodDto?>? {
            arguments = listOf(apiKey, startDate, endDate)
            failure?.let { throw it }
            return response
        }
    }

    // Doble para probar el Repository; no sustituye una prueba instrumental de SQLite.
    private class MemoryDao : ApodDao() {
        private val rows = MutableStateFlow<Map<String, ApodEntity>>(emptyMap())
        override fun observeRange(startDate: String, endDate: String): Flow<List<ApodEntity>> =
            rows.map { it.values.filter { row -> row.date >= startDate && row.date <= endDate }.sortedByDescending { row -> row.date } }
        override fun observeFavorites(): Flow<List<ApodEntity>> =
            rows.map { it.values.filter { row -> row.isFavorite }.sortedByDescending { row -> row.date } }
        override fun observeByDate(date: String): Flow<ApodEntity?> = rows.map { it[date] }
        override suspend fun toggleFavorite(date: String) {
            rows.value[date]?.let { rows.value = rows.value + (date to it.copy(isFavorite = !it.isFavorite)) }
        }
        override suspend fun favoriteDates(): List<String> = rows.value.values.filter { it.isFavorite }.map { it.date }
        override suspend fun upsert(items: List<ApodEntity>) { rows.value = rows.value + items.associateBy { it.date } }
    }
}
