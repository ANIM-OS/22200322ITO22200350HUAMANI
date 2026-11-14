package dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.remote

import com.google.gson.annotations.SerializedName
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import org.jsoup.Jsoup
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
    val credit: String? = null,
)

fun ApodDto.toEntityOrNull(): ApodEntity? {
    val validDate = date?.trim()?.let {
        try { LocalDate.parse(it).toString() } catch (_: DateTimeParseException) { null }
    } ?: return null // Sin fecha válida no existe una clave primaria confiable.
    val entity = ApodEntity(
        date = validDate,
        title = title.plainText() ?: "Sin título",
        explanation = explanation.plainText()?.removePrefix("Explanation:")?.trim() ?: "Explicación no disponible.",
        mediaType = when (mediaType.clean()?.lowercase()) {
            "image" -> "image"
            "video", "iframe" -> "video"
            else -> "unknown"
        },
        url = url.validWebUrl()?.takeUnless { mediaType.clean()?.lowercase() == "image" && it.isNasaArticle() },
        hdUrl = hdUrl.validWebUrl(),
        copyright = copyright.plainText() ?: credit.plainText(),
    )
    return entity.takeUnless { it.isNasaPlaceholder() }
}

/** El parser antiguo devuelve el logo de NASA Science como si fuera una foto APOD. */
fun ApodEntity.isNasaPlaceholder(): Boolean = listOfNotNull(url, hdUrl).any {
    runCatching {
        val uri = URI(it)
        uri.host?.endsWith("nasa.gov") == true && uri.path.contains("nasa-logo", ignoreCase = true)
    }.getOrDefault(false)
}

private fun String.isNasaArticle(): Boolean = runCatching {
    val uri = URI(this)
    uri.host == "science.nasa.gov" && (uri.path.startsWith("/image-article/") || uri.path == "/apod/")
}.getOrDefault(false)

private fun String?.plainText(): String? = clean()?.let { Jsoup.parse(it).text().trim().takeIf(String::isNotEmpty) }

private fun String?.clean(): String? = this?.trim()?.takeIf { it.isNotEmpty() }

private fun String?.validWebUrl(): String? = clean()?.takeIf {
    runCatching {
        val uri = URI(it)
        uri.scheme?.lowercase() in setOf("https", "http") && !uri.host.isNullOrBlank()
    }.getOrDefault(false)
}
