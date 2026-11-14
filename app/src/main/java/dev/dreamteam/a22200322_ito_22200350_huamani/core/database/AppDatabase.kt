package dev.dreamteam.a22200322_ito_22200350_huamani.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodDao
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity

@Database(entities = [ApodEntity::class], version = 1, exportSchema = true)
abstract class AppDatabase : RoomDatabase() {
    abstract fun apodDao(): ApodDao

    companion object {
        @Volatile private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext, AppDatabase::class.java, "nasa.db"
            ).build().also { instance = it }
        }
    }
}
