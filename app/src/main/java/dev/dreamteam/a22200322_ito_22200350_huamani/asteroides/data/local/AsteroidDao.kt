package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AsteroidDao {
    @Query("SELECT * FROM asteroids WHERE approachDate BETWEEN :startDate AND :endDate ORDER BY approachDate DESC, name ASC")
    fun observeRecent(startDate: String, endDate: String): Flow<List<AsteroidEntity>>

    @Query("SELECT * FROM asteroids WHERE isFavorite = 1 ORDER BY approachDate DESC, name ASC")
    fun observeFavorites(): Flow<List<AsteroidEntity>>

    @Query("SELECT * FROM asteroids WHERE id = :id LIMIT 1")
    fun observeById(id: String): Flow<AsteroidEntity?>

    @Query("SELECT id FROM asteroids WHERE isFavorite = 1")
    suspend fun favoriteIds(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asteroids: List<AsteroidEntity>)

    @Query("DELETE FROM asteroids WHERE isFavorite = 0")
    suspend fun removeNonFavorites()

    @Query("UPDATE asteroids SET isFavorite = NOT isFavorite WHERE id = :id")
    suspend fun toggleFavorite(id: String)

    @Transaction
    suspend fun saveFeedPreservingFavorites(
        asteroids: List<AsteroidEntity>,
    ) {
        val favorites = favoriteIds().toSet()
        removeNonFavorites()
        insertAll(asteroids.map { it.copy(isFavorite = it.id in favorites) })
    }
}
