package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote

import com.google.gson.annotations.SerializedName
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import java.net.URI
import java.time.LocalDate
import java.time.format.DateTimeParseException

// Todos los campos de la API pueden estar ausentes o ser null.
data class ApodDto(
    val date: String? = null,
    val title: String? = null,
    val explanation: String? = null,
    @SerializedName("media_type") val mediaType: String? = null,
    val url: String? = null,
    @SerializedName("hdurl") val hdUrl: String? = null,
    val copyright: String? = null,
)

fun ApodDto.toEntityOrNull(): ApodEntity? {
    val validDate = date?.trim()?.let {
        try { LocalDate.parse(it).toString() } catch (_: DateTimeParseException) { null }
    } ?: return null // Sin fecha válida no existe una clave primaria confiable.
    return ApodEntity(
        date = validDate,
        title = title.clean() ?: "Sin título",
        explanation = explanation.clean() ?: "Explicación no disponible.",
        mediaType = when (mediaType.clean()?.lowercase()) {
            "image" -> "image"
            "video" -> "video"
            else -> "unknown"
        },
        url = url.validWebUrl(),
        hdUrl = hdUrl.validWebUrl(),
        copyright = copyright.clean(),
    )
}

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun String?.validWebUrl(): String? = clean()?.takeIf {
    runCatching {
        val uri = URI(it)
        uri.scheme?.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
