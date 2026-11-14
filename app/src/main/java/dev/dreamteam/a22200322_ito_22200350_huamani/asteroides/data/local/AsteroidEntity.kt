package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "asteroids")
data class AsteroidEntity(
    @PrimaryKey val id: String,
    val name: String,
    /** ISO 8601 date (yyyy-MM-dd), allowing chronological SQLite sorting. */
    val approachDate: String,
    val diameterMinMeters: Double?,
    val diameterMaxMeters: Double?,
    val velocityKmH: Double?,
    val distanceKm: Double?,
    val isPotentiallyHazardous: Boolean?,
    val isFavorite: Boolean = false,
)
