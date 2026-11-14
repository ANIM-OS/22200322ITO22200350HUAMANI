package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import java.text.NumberFormat

@Composable
fun AsteroidListScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onFavoritesClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Asteroides · últimos 7 días", style = MaterialTheme.typography.titleLarge, modifier = Modifier.weight(1f))
            TextButton(onClick = onFavoritesClick) { Text("Mis asteroides") }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Solo potencialmente peligrosos", modifier = Modifier.weight(1f))
            Switch(checked = state.onlyHazardous, onCheckedChange = viewModel::setOnlyHazardous)
        }
        Button(onClick = viewModel::refresh, enabled = !state.isLoading) { Text("Actualizar") }
        if (state.isLoading) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                Text("Cargando asteroides…")
            }
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.asteroids.isEmpty() && !state.isLoading) {
            Text(
                if (state.onlyHazardous) "No hay asteroides potencialmente peligrosos en este periodo."
                else "No hay asteroides para los últimos 7 días.",
                modifier = Modifier.padding(top = 16.dp),
            )
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.asteroids, key = { it.id }) { asteroid ->
                AsteroidRow(asteroid, onAsteroidClick, viewModel::toggleFavorite)
            }
        }
    }
}

@Composable
fun FavoriteAsteroidsScreen(
    viewModel: AsteroidViewModel,
    onAsteroidClick: (String) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = onBack) { Text("Volver") }
            Text("Mis asteroides", style = MaterialTheme.typography.titleLarge)
        }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (state.isLoading && state.favorites.isEmpty()) {
            CircularProgressIndicator()
        } else if (state.favorites.isEmpty()) {
            Text("Aún no tienes asteroides favoritos.", modifier = Modifier.padding(top = 16.dp))
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.favorites, key = { it.id }) { asteroid ->
                AsteroidRow(asteroid, onAsteroidClick, viewModel::toggleFavorite)
            }
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
    val detail by detailFlow.collectAsState()
    val state by viewModel.uiState.collectAsState()
    Column(modifier = modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        TextButton(onClick = onBack) { Text("Volver") }
        state.errorMessage?.let { Text(it, color = MaterialTheme.colorScheme.error) }
        if (detail.isLoading) {
            CircularProgressIndicator()
        } else {
            val asteroid = detail.asteroid
            if (asteroid == null) {
                Text("No se encontró este asteroide en la base local.")
            } else {
                Text(asteroid.name, style = MaterialTheme.typography.headlineSmall)
                Text("ID: ${asteroid.id}")
                Text("Fecha de aproximación: ${asteroid.approachDate}")
                Text("Diámetro mínimo: ${formatMetric(asteroid.diameterMinMeters, "m")}")
                Text("Diámetro máximo: ${formatMetric(asteroid.diameterMaxMeters, "m")}")
                Text("Velocidad: ${formatMetric(asteroid.velocityKmH, "km/h")}")
                Text("Distancia: ${formatMetric(asteroid.distanceKm, "km")}")
                HazardLabel(asteroid.isPotentiallyHazardous)
                Button(onClick = { viewModel.toggleFavorite(asteroid.id) }) {
                    Text(if (asteroid.isFavorite) "Quitar de Mis asteroides" else "Agregar a Mis asteroides")
                }
            }
        }
    }
}

@Composable
private fun AsteroidRow(
    asteroid: AsteroidEntity,
    onClick: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Card(modifier = Modifier.fillMaxWidth().clickable { onClick(asteroid.id) }) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(asteroid.name, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Aproximación: ${asteroid.approachDate}")
            Text("Diámetro máximo: ${formatMetric(asteroid.diameterMaxMeters, "m")}")
            Text("Velocidad: ${formatMetric(asteroid.velocityKmH, "km/h")}")
            Text("Distancia: ${formatMetric(asteroid.distanceKm, "km")}")
            HazardLabel(asteroid.isPotentiallyHazardous)
            TextButton(onClick = { onToggleFavorite(asteroid.id) }) {
                Text(if (asteroid.isFavorite) "★ Quitar favorito" else "☆ Guardar favorito")
            }
        }
    }
}

@Composable
private fun HazardLabel(isHazardous: Boolean?) {
    Text(
        when (isHazardous) {
            true -> "⚠ Potencialmente peligroso"
            false -> "No clasificado como peligroso"
            null -> "Peligrosidad no disponible"
        },
        color = if (isHazardous == true) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = if (isHazardous == true) FontWeight.Bold else FontWeight.Normal,
    )
}

private fun formatMetric(value: Double?, unit: String): String {
    if (value == null) return "Dato no disponible"
    val formatter = NumberFormat.getNumberInstance().apply {
        maximumFractionDigits = 2
        minimumFractionDigits = 0
    }
    return "${formatter.format(value)} $unit"
}
