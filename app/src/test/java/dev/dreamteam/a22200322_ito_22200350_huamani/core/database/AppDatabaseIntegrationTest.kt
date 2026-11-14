package dev.dreamteam.a22200322_ito_22200350_huamani.core.database

import android.content.Context
import androidx.room.Room
import com.google.gson.JsonParser
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.annotation.SQLiteMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
@SQLiteMode(SQLiteMode.Mode.NATIVE)
class AppDatabaseIntegrationTest {
    private val context: Context get() = RuntimeEnvironment.getApplication()
    private val databases = mutableListOf<AppDatabase>()
    private val databaseName = "nasa-migration-test.db"

    @After fun cleanup() {
        databases.forEach { it.close() }
        context.deleteDatabase(databaseName)
    }

    @Test fun migrationFromPublishedV1KeepsApodFavoritesAndAddsAsteroids() = runBlocking {
        // Construir exactamente la versión publicada antes de la integración.
        val resource = requireNotNull(javaClass.classLoader?.getResourceAsStream("${AppDatabase::class.java.name}/1.json"))
        val schema = resource.reader().use { JsonParser.parseReader(it).asJsonObject.getAsJsonObject("database") }
        context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null).use { old ->
            schema.getAsJsonArray("entities").forEach {
                val entity = it.asJsonObject
                old.execSQL(entity.get("createSql").asString.replace("\${TABLE_NAME}", entity.get("tableName").asString))
            }
            schema.getAsJsonArray("setupQueries").forEach { old.execSQL(it.asString) }
            old.execSQL(
                "INSERT INTO apod (date,title,explanation,mediaType,isFavorite) VALUES (?,?,?,?,?)",
                arrayOf("2026-11-14", "Favorito anterior", "Texto completo", "video", 1),
            )
            old.version = 1
        }
        val database = openDiskDatabase()
        assertEquals(2, database.openHelper.writableDatabase.version) // Room valida el esquema de ambas tablas.
        val favorite = withTimeout(5_000) { database.apodDao().observeFavorites().first() }.single()
        assertEquals("Favorito anterior", favorite.title)
        assertTrue(favorite.isFavorite)
        database.asteroidDao().insertAll(listOf(asteroid("1", "2026-11-14")))
        assertEquals("1", withTimeout(5_000) { database.asteroidDao().observeById("1").first() }?.id)
    }

    @Test fun bothDaosPreserveTheirOwnFavoritesAndSortRecentRows() = runBlocking {
        val database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries().build().also { databases.add(it) }
        val apod = database.apodDao()
        val asteroids = database.asteroidDao()
        apod.mergeFromApi(listOf(image("2026-11-13"), image("2026-11-14")))
        asteroids.insertAll(listOf(asteroid("1", "2026-11-14"), asteroid("2", "2026-11-13")))
        apod.toggleFavorite("2026-11-14")
        asteroids.toggleFavorite("1")
        apod.mergeFromApi(listOf(image("2026-11-14").copy(title = "APOD actualizado")))
        asteroids.saveFeedPreservingFavorites(listOf(asteroid("1", "2026-11-14").copy(name = "Asteroide actualizado"), asteroid("2", "2026-11-13")))
        assertEquals(listOf("2026-11-14", "2026-11-13"), withTimeout(5_000) { apod.observeRange("2026-11-08", "2026-11-14").first() }.map { it.date })
        assertEquals(listOf("1", "2"), withTimeout(5_000) { asteroids.observeRecent("2026-11-08", "2026-11-14").first() }.map { it.id })
        assertEquals("APOD actualizado", withTimeout(5_000) { apod.observeFavorites().first() }.single().title)
        assertEquals("Asteroide actualizado", withTimeout(5_000) { asteroids.observeFavorites().first() }.single().name)
        apod.toggleFavorite("2026-11-14")
        assertTrue(withTimeout(5_000) { apod.observeFavorites().first() }.isEmpty())
        assertEquals(1, withTimeout(5_000) { asteroids.observeFavorites().first() }.size)
    }

    @Test fun favoritesFromBothModulesSurviveDatabaseReopening() = runBlocking {
        var database = openDiskDatabase()
        database.apodDao().mergeFromApi(listOf(image("2026-11-14")))
        database.asteroidDao().insertAll(listOf(asteroid("1", "2026-11-14")))
        database.apodDao().toggleFavorite("2026-11-14")
        database.asteroidDao().toggleFavorite("1")
        database.close()
        database = openDiskDatabase()
        assertTrue(withTimeout(5_000) { database.apodDao().observeByDate("2026-11-14").first() }!!.isFavorite)
        assertTrue(withTimeout(5_000) { database.asteroidDao().observeById("1").first() }!!.isFavorite)
    }

    private fun openDiskDatabase(): AppDatabase = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
        .addMigrations(AppDatabase.MIGRATION_1_2).allowMainThreadQueries().build().also { databases.add(it) }

    private fun image(date: String) = ApodEntity(date, "Imagen", "Explicación", "video", null, null, null)
    private fun asteroid(id: String, date: String) = AsteroidEntity(id, "Asteroide", date, null, null, null, null, null)
}
