package dev.dreamteam.a22200322_ito_22200350_huamani

import android.content.Context
import androidx.room.Room
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.core.database.AppDatabase
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    @Test
    fun migrationKeepsApodFavoriteAndEnablesAsteroidDao() = runBlocking {
        val context = InstrumentationRegistry.getInstrumentation().targetContext
        val databaseName = "migration-test-${System.nanoTime()}.db"
        val oldDatabase = context.openOrCreateDatabase(databaseName, Context.MODE_PRIVATE, null)
        oldDatabase.execSQL(
            """CREATE TABLE IF NOT EXISTS `apod` (
                `date` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `explanation` TEXT NOT NULL,
                `mediaType` TEXT NOT NULL,
                `url` TEXT,
                `hdUrl` TEXT,
                `copyright` TEXT,
                `isFavorite` INTEGER NOT NULL,
                PRIMARY KEY(`date`)
            )""".trimIndent()
        )
        oldDatabase.execSQL(
            "INSERT INTO apod (date, title, explanation, mediaType, url, hdUrl, copyright, isFavorite) " +
                "VALUES ('2026-11-14', 'Imagen', 'Descripción', 'image', NULL, NULL, NULL, 1)"
        )
        oldDatabase.version = 1
        oldDatabase.close()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(AppDatabase.MIGRATION_1_2)
            .build()
        try {
            // Opening the v1 file also validates the entire generated v2 schema.
            assertTrue(database.apodDao().observeByDate("2026-11-14").first()?.isFavorite == true)

            val asteroid = AsteroidEntity(
                id = "123", name = "Asteroide", approachDate = "2026-11-14",
                diameterMinMeters = 1.0, diameterMaxMeters = 2.0,
                velocityKmH = 3.0, distanceKm = 4.0,
                isPotentiallyHazardous = true,
            )
            database.asteroidDao().insertAll(listOf(asteroid))
            database.asteroidDao().toggleFavorite(asteroid.id)
            database.asteroidDao().saveFeedPreservingFavorites(listOf(asteroid.copy(name = "Actualizado")))
            val updated = database.asteroidDao().observeById(asteroid.id).first()
            assertEquals("Actualizado", updated?.name)
            assertTrue(updated?.isFavorite == true)
            assertEquals(1, database.asteroidDao().observeFavorites().first().size)
        } finally {
            database.close()
            context.deleteDatabase(databaseName)
        }
    }
}
