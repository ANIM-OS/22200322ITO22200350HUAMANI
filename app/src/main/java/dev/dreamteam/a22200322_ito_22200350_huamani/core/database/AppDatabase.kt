package dev.dreamteam.a22200322_ito_22200350_huamani.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodDao
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidDao
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity

@Database(entities = [ApodEntity::class, AsteroidEntity::class], version = 2, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao
    abstract fun asteroidDao(): AsteroidDao

    companion object {
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    """CREATE TABLE IF NOT EXISTS `asteroids` (
                        `id` TEXT NOT NULL,
                        `name` TEXT NOT NULL,
                        `approachDate` TEXT NOT NULL,
                        `diameterMinMeters` REAL,
                        `diameterMaxMeters` REAL,
                        `velocityKmH` REAL,
                        `distanceKm` REAL,
                        `isPotentiallyHazardous` INTEGER,
                        `isFavorite` INTEGER NOT NULL,
                        PRIMARY KEY(`id`)
                    )""".trimIndent()
                )
            }
        }

        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "nasa.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }
    }
}
