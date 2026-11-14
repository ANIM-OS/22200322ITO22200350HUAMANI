package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.remote

import com.google.gson.annotations.SerializedName
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import java.time.LocalDate

data class AsteroidFeedDto(
    @SerializedName("near_earth_objects") val nearEarthObjects: Map<String, List<AsteroidDto?>?>? = null,
)

data class AsteroidDto(
    @SerializedName("id") val id: String? = null,
    @SerializedName("name") val name: String? = null,
    @SerializedName("is_potentially_hazardous_asteroid") val isPotentiallyHazardous: Boolean? = null,
    @SerializedName("estimated_diameter") val estimatedDiameter: EstimatedDiameterDto? = null,
    @SerializedName("close_approach_data") val closeApproachData: List<CloseApproachDto?>? = null,
)

data class EstimatedDiameterDto(
    @SerializedName("meters") val meters: DiameterMetersDto? = null,
)

data class DiameterMetersDto(
    @SerializedName("estimated_diameter_min") val min: Double? = null,
    @SerializedName("estimated_diameter_max") val max: Double? = null,
)

data class CloseApproachDto(
    @SerializedName("close_approach_date") val date: String? = null,
    @SerializedName("relative_velocity") val relativeVelocity: RelativeVelocityDto? = null,
    @SerializedName("miss_distance") val missDistance: MissDistanceDto? = null,
)

data class RelativeVelocityDto(
    @SerializedName("kilometers_per_hour") val kilometersPerHour: String? = null,
)

data class MissDistanceDto(
    @SerializedName("kilometers") val kilometers: String? = null,
)

/** Returns null when the NASA ID or feed date cannot identify a usable record. */
fun AsteroidDto.toEntity(feedDate: String): AsteroidEntity? {
    val asteroidId = id?.trim()?.takeIf(String::isNotEmpty) ?: return null
    val date = runCatching { LocalDate.parse(feedDate) }.getOrNull() ?: return null
    val approach = closeApproachData.orEmpty().firstOrNull { it?.date == feedDate }
    val meters = estimatedDiameter?.meters

    return AsteroidEntity(
        id = asteroidId,
        name = name?.trim()?.takeIf(String::isNotEmpty) ?: "Sin nombre",
        approachDate = date.toString(),
        diameterMinMeters = meters?.min.validNonNegative(),
        diameterMaxMeters = meters?.max.validNonNegative(),
        velocityKmH = approach?.relativeVelocity?.kilometersPerHour.toValidNonNegativeDouble(),
        distanceKm = approach?.missDistance?.kilometers.toValidNonNegativeDouble(),
        isPotentiallyHazardous = isPotentiallyHazardous,
    )
}

private fun Double?.validNonNegative(): Double? = this?.takeIf { it.isFinite() && it >= 0.0 }

private fun String?.toValidNonNegativeDouble(): Double? =
    this?.toDoubleOrNull().validNonNegative()
