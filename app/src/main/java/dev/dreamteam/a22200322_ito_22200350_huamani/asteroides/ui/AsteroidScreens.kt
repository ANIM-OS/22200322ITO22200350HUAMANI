package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.core.ui.SpaceIcon
import dev.dreamteam.a22200322_ito_22200350_huamani.core.ui.SpaceSymbol
import java.text.NumberFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun AsteroidListScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AsteroidCollectionScreen(
        state = state,
        favoritesOnly = false,
        onOpenDetail = onAsteroidClick,
        onOpenFavorites = onFavoritesClick,
        onBack = {},
        onRefresh = viewModel::refresh,
        onToggleFavorite = viewModel::toggleFavorite,
        onHazardousOnlyChange = viewModel::setOnlyHazardous,
        modifier = modifier,
    )
}

@Composable
fun FavoriteAsteroidsScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    AsteroidCollectionScreen(
        state = state,
        favoritesOnly = true,
        onOpenDetail = onAsteroidClick,
        onOpenFavorites = {},
        onBack = onBack,
        onRefresh = {},
        onToggleFavorite = viewModel::toggleFavorite,
        onHazardousOnlyChange = {},
        modifier = modifier,
    )
}

@Composable
private fun AsteroidCollectionScreen(
    state: AsteroidUiState,
    favoritesOnly: Boolean,
    onOpenDetail: (String) -> Unit,
    onOpenFavorites: () -> Unit,
    onBack: () -> Unit,
    onRefresh: () -> Unit,
    onToggleFavorite: (String) -> Unit,
    onHazardousOnlyChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val asteroids = if (favoritesOnly) state.favorites else state.asteroids
    Column(modifier.fillMaxSize()) {
        CollectionHeader(favoritesOnly, state.isLoading, onBack, onOpenFavorites, onRefresh)
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 28.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            item {
                Column(Modifier.padding(top = 18.dp, bottom = 4.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        if (favoritesOnly) "TUS FAVORITOS" else "EXPLORA · NASA NEOWS",
                        style = MaterialTheme.typography.labelSmall,
                        letterSpacing = 2.sp,
                        color = MaterialTheme.colorScheme.secondary,
                    )
                    Text(
                        if (favoritesOnly) "Mis asteroides\nfavoritos" else "Explora los\nasteroides",
                        fontSize = 36.sp,
                        lineHeight = 41.sp,
                        fontWeight = FontWeight.SemiBold,
                        letterSpacing = (-1).sp,
                    )
                    Text(
                        if (favoritesOnly) "Los objetos cercanos a la Tierra que elegiste guardar."
                        else "Descubre los objetos cercanos a la Tierra que se aproximaron esta semana.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        LabelPill(if (favoritesOnly) countLabel(asteroids.size, "guardado", "guardados") else "Últimos 7 días")
                        if (!favoritesOnly) LabelPill(countLabel(asteroids.size, "asteroide", "asteroides"), secondary = true)
                    }
                }
            }
            if (!favoritesOnly) {
                item {
                    HazardFilter(state.onlyHazardous, onHazardousOnlyChange)
                }
            }
            state.errorMessage?.let { message ->
                item { MessageBanner(message, if (!favoritesOnly && !state.isLoading) onRefresh else null) }
            }
            if (state.isLoading && asteroids.isNotEmpty()) {
                item { LinearProgressIndicator(Modifier.fillMaxWidth().clip(CircleShape)) }
            }
            when {
                state.isLoading && asteroids.isEmpty() -> item { LoadingContent() }
                asteroids.isEmpty() -> item {
                    EmptyContent(
                        when {
                            favoritesOnly -> "Todavía no guardaste asteroides. Toca el corazón de una tarjeta para encontrarlos aquí."
                            state.onlyHazardous -> "No hay asteroides potencialmente peligrosos en este periodo."
                            else -> "No hay asteroides disponibles para estos días. Puedes volver a consultar NASA."
                        },
                        if (favoritesOnly) null else onRefresh,
                    )
                }
                else -> {
                    item {
                        Row(Modifier.fillMaxWidth().padding(top = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (favoritesOnly) "Tu colección" else "Aproximaciones recientes", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text(countLabel(asteroids.size, "objeto", "objetos"), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    items(asteroids, key = { it.id }) { asteroid ->
                        AsteroidCard(
                            asteroid = asteroid,
                            onOpen = { onOpenDetail(asteroid.id) },
                            onToggleFavorite = { onToggleFavorite(asteroid.id) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CollectionHeader(
    favoritesOnly: Boolean,
    isLoading: Boolean,
    onBack: () -> Unit,
    onOpenFavorites: () -> Unit,
    onRefresh: () -> Unit,
) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
        if (favoritesOnly) RoundAction(SpaceSymbol.Back, "Volver", onBack)
        else SpaceIcon(SpaceSymbol.Orbit, Modifier.size(30.dp), MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text("DIARIO ESPACIAL", style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            Text(
                if (favoritesOnly) "Tu colección del universo" else "Cerca de la Tierra",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
        if (!favoritesOnly) {
            RoundAction(SpaceSymbol.Heart, "Mis asteroides favoritos", onOpenFavorites)
            Spacer(Modifier.width(6.dp))
            RoundAction(SpaceSymbol.Refresh, "Actualizar asteroides", onRefresh, enabled = !isLoading)
        }
    }
}

@Composable
private fun HazardFilter(checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
    ) {
        Row(Modifier.fillMaxWidth().padding(start = 16.dp, end = 10.dp, top = 10.dp, bottom = 10.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
                SpaceIcon(SpaceSymbol.Orbit, Modifier.padding(9.dp).size(20.dp), MaterialTheme.colorScheme.primary)
            }
            Column(Modifier.weight(1f).padding(start = 12.dp)) {
                Text("Solo potencialmente peligrosos", style = MaterialTheme.typography.labelLarge, maxLines = 2)
                Text("Según la clasificación de NASA", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.semantics { contentDescription = "Solo potencialmente peligrosos" },
            )
        }
    }
}

@Composable
private fun AsteroidCard(asteroid: AsteroidEntity, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Ver detalle de ${asteroid.name}", onClick = onOpen),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f)),
    ) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(13.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(shape = RoundedCornerShape(16.dp), color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)) {
                    SpaceIcon(SpaceSymbol.Orbit, Modifier.padding(11.dp).size(24.dp), MaterialTheme.colorScheme.primary)
                }
                Column(Modifier.weight(1f).padding(start = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(asteroid.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 2, overflow = TextOverflow.Ellipsis)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SpaceIcon(SpaceSymbol.Calendar, Modifier.size(14.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(formatDate(asteroid.approachDate), Modifier.padding(start = 6.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
                FavoriteButton(asteroid, onToggleFavorite)
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                MetricBlock("DIÁMETRO MÁX.", asteroid.diameterMaxMeters, "m", Modifier.weight(1f))
                MetricBlock("VELOCIDAD", asteroid.velocityKmH, "km/h", Modifier.weight(1f))
            }
            Row(verticalAlignment = Alignment.Bottom) {
                MetricBlock("DISTANCIA A LA TIERRA", asteroid.distanceKm, "km", Modifier.weight(1f))
                SpaceIcon(SpaceSymbol.Arrow, Modifier.size(20.dp), MaterialTheme.colorScheme.primary)
            }
            HazardPill(asteroid.isPotentiallyHazardous)
        }
    }
}

@Composable
fun AsteroidDetailScreen(
    viewModel: AsteroidViewModel,
    asteroidId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val detailFlow = remember(viewModel, asteroidId) { viewModel.observeDetail(asteroidId) }
    val detail by detailFlow.collectAsStateWithLifecycle()
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val asteroid = detail.asteroid
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundAction(SpaceSymbol.Back, "Volver", onBack)
            Text("EXPLORACIÓN NEOWS", Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            if (asteroid != null) FavoriteButton(asteroid) { viewModel.toggleFavorite(asteroid.id) }
            else Spacer(Modifier.width(44.dp))
        }
        state.errorMessage?.let { MessageBanner(it) }
        when {
            detail.isLoading -> LoadingContent()
            asteroid == null -> EmptyContent("Este asteroide no está disponible en la base local. Vuelve a la lista para consultar los datos guardados.")
            else -> Column(
                Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp),
            ) {
                OrbitalIllustration(Modifier.fillMaxWidth().height(170.dp))
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    Text("OBJETO CERCANO A LA TIERRA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary, letterSpacing = 2.sp)
                    Text(asteroid.name, fontSize = 32.sp, lineHeight = 38.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.7).sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        SpaceIcon(SpaceSymbol.Calendar, Modifier.size(18.dp), MaterialTheme.colorScheme.secondary)
                        Text(formatDate(asteroid.approachDate), Modifier.padding(start = 8.dp), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.secondary)
                    }
                    Text("ID NASA · ${asteroid.id}", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    HazardPill(asteroid.isPotentiallyHazardous)
                }
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                Text("Dimensiones estimadas", style = MaterialTheme.typography.titleMedium)
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    MetricTile("DIÁMETRO MÍNIMO", asteroid.diameterMinMeters, "m", Modifier.weight(1f))
                    MetricTile("DIÁMETRO MÁXIMO", asteroid.diameterMaxMeters, "m", Modifier.weight(1f))
                }
                Text("Aproximación a la Tierra", style = MaterialTheme.typography.titleMedium)
                MetricTile("VELOCIDAD RELATIVA", asteroid.velocityKmH, "km/h", Modifier.fillMaxWidth())
                MetricTile("DISTANCIA DE PASO", asteroid.distanceKm, "km", Modifier.fillMaxWidth())
                Text("DATOS DE NASA · NEOWS", Modifier.padding(bottom = 28.dp), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
            }
        }
    }
}

@Composable
private fun OrbitalIllustration(modifier: Modifier = Modifier) {
    val primary = MaterialTheme.colorScheme.primary
    val secondary = MaterialTheme.colorScheme.secondary
    Box(modifier.clip(RoundedCornerShape(28.dp)).background(MaterialTheme.colorScheme.surfaceContainerHigh), contentAlignment = Alignment.Center) {
        Canvas(Modifier.fillMaxSize()) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val radius = size.minDimension * 0.29f
            drawCircle(primary.copy(alpha = 0.08f), radius * 1.7f, center)
            drawCircle(primary.copy(alpha = 0.25f), radius, center, style = Stroke(width = 1.5.dp.toPx()))
            drawCircle(secondary.copy(alpha = 0.36f), radius * 1.45f, center, style = Stroke(width = 1.dp.toPx()))
            drawCircle(secondary, 4.dp.toPx(), Offset(center.x + radius * 1.35f, center.y - radius * 0.55f))
        }
        Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer) {
            SpaceIcon(SpaceSymbol.Orbit, Modifier.padding(22.dp).size(48.dp), primary)
        }
    }
}

@Composable
private fun MetricTile(label: String, value: Double?, unit: String, modifier: Modifier = Modifier) {
    Surface(modifier, shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))) {
        MetricBlock(label, value, unit, Modifier.padding(16.dp))
    }
}

@Composable
private fun MetricBlock(label: String, value: Double?, unit: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        Text(label, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 0.7.sp, maxLines = 2)
        Text(formatNumber(value), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold, maxLines = 1, overflow = TextOverflow.Ellipsis)
        Text(if (value == null) "Dato no disponible" else unit, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
    }
}

@Composable
private fun HazardPill(isHazardous: Boolean?) {
    val label = when (isHazardous) {
        true -> "Potencialmente peligroso"
        false -> "No clasificado como peligroso"
        null -> "Peligrosidad no disponible"
    }
    val background = when (isHazardous) {
        true -> MaterialTheme.colorScheme.errorContainer
        false -> MaterialTheme.colorScheme.secondaryContainer
        null -> MaterialTheme.colorScheme.surfaceContainerHigh
    }
    val foreground = when (isHazardous) {
        true -> MaterialTheme.colorScheme.error
        false -> MaterialTheme.colorScheme.secondary
        null -> MaterialTheme.colorScheme.onSurfaceVariant
    }
    Surface(shape = CircleShape, color = background) {
        Text(label, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = foreground, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun FavoriteButton(asteroid: AsteroidEntity, onToggle: () -> Unit) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh) {
        IconToggleButton(
            checked = asteroid.isFavorite,
            onCheckedChange = { onToggle() },
            modifier = Modifier.size(44.dp).semantics {
                contentDescription = if (asteroid.isFavorite) "Quitar de favoritos: ${asteroid.name}" else "Agregar a favoritos: ${asteroid.name}"
            },
        ) {
            SpaceIcon(SpaceSymbol.Heart, Modifier.size(21.dp),
                if (asteroid.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                filled = asteroid.isFavorite)
        }
    }
}

@Composable
private fun RoundAction(symbol: SpaceSymbol, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp).semantics { contentDescription = label }) {
            SpaceIcon(symbol, Modifier.size(21.dp),
                if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun LabelPill(text: String, secondary: Boolean = false) {
    Surface(shape = CircleShape, color = if (secondary) MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.55f)
        else MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
            color = if (secondary) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.primary,
            style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MessageBanner(message: String, onRetry: (() -> Unit)? = null) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.65f),
        shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.25f))) {
        Column(Modifier.padding(16.dp)) {
            Text(message, style = MaterialTheme.typography.bodySmall, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onErrorContainer)
            onRetry?.let { TextButton(onClick = it) { Text("Volver a intentar") } }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(Modifier.fillMaxWidth().padding(vertical = 56.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp)
        Spacer(Modifier.height(18.dp))
        Text("Buscando asteroides cercanos…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyContent(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 44.dp, horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SpaceIcon(SpaceSymbol.Orbit, Modifier.size(52.dp), MaterialTheme.colorScheme.primary)
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        onRetry?.let { FilledTonalButton(onClick = it) { Text("Actualizar asteroides") } }
    }
}

private fun formatNumber(value: Double?): String = if (value == null) "—" else NumberFormat.getNumberInstance().apply {
    maximumFractionDigits = 2
    minimumFractionDigits = 0
}.format(value)

private fun countLabel(count: Int, singular: String, plural: String): String =
    "$count ${if (count == 1) singular else plural}"

private fun formatDate(date: String): String = runCatching {
    LocalDate.parse(date).format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale.forLanguageTag("es-PE")))
}.getOrDefault(date)
