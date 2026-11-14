package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "apod")
data class ApodEntity(
    @PrimaryKey val date: String,
    val title: String,
    val explanation: String,
    val mediaType: String,
    val url: String?,
    val hdUrl: String?,
    val copyright: String?,
    val isFavorite: Boolean = false,
)
