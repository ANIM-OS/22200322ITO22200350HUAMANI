package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
abstract class ApodDao {
    @Query("SELECT * FROM apod WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    abstract fun observeRange(startDate: String, endDate: String): Flow<List<ApodEntity>>

    @Query("SELECT * FROM apod WHERE isFavorite = 1 ORDER BY date DESC")
    abstract fun observeFavorites(): Flow<List<ApodEntity>>

    @Query("SELECT * FROM apod WHERE date = :date")
    abstract fun observeByDate(date: String): Flow<ApodEntity?>

    @Query("UPDATE apod SET isFavorite = NOT isFavorite WHERE date = :date")
    abstract suspend fun toggleFavorite(date: String)

    @Query("SELECT date FROM apod WHERE isFavorite = 1")
    protected abstract suspend fun favoriteDates(): List<String>

    @Upsert
    protected abstract suspend fun upsert(items: List<ApodEntity>)

    // Una actualización de la API nunca debe borrar la selección local de favoritos.
    @Transaction
    open suspend fun mergeFromApi(items: List<ApodEntity>) {
        val favorites = favoriteDates().toSet()
        upsert(items.map { it.copy(isFavorite = it.date in favorites) })
    }
}
