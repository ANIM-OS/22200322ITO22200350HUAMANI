package dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.SubcomposeAsyncImage
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import dev.dreamteam.a22200322_ito_22200350_huamani.core.ui.SpaceIcon
import dev.dreamteam.a22200322_ito_22200350_huamani.core.ui.SpaceSymbol
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun ApodListRoute(
    viewModel: ApodViewModel,
    onOpenFavorites: () -> Unit,
    onOpenDetail: (String) -> Unit,
) {
    val state by viewModel.images.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    LaunchedEffect(viewModel) { viewModel.refresh() }
    ApodListScreen(
        state = state,
        favoritesOnly = false,
        actionError = actionError,
        onDismissError = viewModel::dismissActionError,
        onRefresh = viewModel::refresh,
        onOpenFavorites = onOpenFavorites,
        onBack = {},
        onOpenDetail = onOpenDetail,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@Composable
fun ApodFavoritesRoute(viewModel: ApodViewModel, onBack: () -> Unit, onOpenDetail: (String) -> Unit) {
    val state by viewModel.favorites.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    ApodListScreen(
        state = state,
        favoritesOnly = true,
        actionError = actionError,
        onDismissError = viewModel::dismissActionError,
        onRefresh = {},
        onOpenFavorites = {},
        onBack = onBack,
        onOpenDetail = onOpenDetail,
        onToggleFavorite = viewModel::toggleFavorite,
    )
}

@Composable
fun ApodDetailRoute(viewModel: ApodViewModel, date: String, onBack: () -> Unit) {
    val detailFlow = remember(viewModel, date) { viewModel.detail(date) }
    val state by detailFlow.collectAsStateWithLifecycle()
    val actionError by viewModel.actionError.collectAsStateWithLifecycle()
    ApodDetailScreen(state, actionError, viewModel::dismissActionError, onBack, viewModel::toggleFavorite)
}

@Composable
private fun ApodListScreen(
    state: ApodListState,
    favoritesOnly: Boolean,
    actionError: String?,
    onDismissError: () -> Unit,
    onRefresh: () -> Unit,
    onOpenFavorites: () -> Unit,
    onBack: () -> Unit,
    onOpenDetail: (String) -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            if (favoritesOnly) RoundAction(SpaceSymbol.Back, "Volver", onBack)
            else SpaceIcon(SpaceSymbol.Orbit, Modifier.size(30.dp), MaterialTheme.colorScheme.primary)
            Spacer(Modifier.width(10.dp))
            Column(Modifier.weight(1f)) {
                Text("DIARIO ESPACIAL", style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp, color = MaterialTheme.colorScheme.onSurface)
                Text(if (favoritesOnly) "Tu colección del universo" else "Un descubrimiento cada día", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (!favoritesOnly) {
                RoundAction(SpaceSymbol.Heart, "Mis imágenes favoritas", onOpenFavorites)
                Spacer(Modifier.width(6.dp))
                RoundAction(SpaceSymbol.Refresh, "Actualizar imágenes", onRefresh, enabled = !state.isLoading)
            }
        }
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(start = 20.dp, end = 20.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                Column(Modifier.padding(top = 18.dp, bottom = 6.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(if (favoritesOnly) "TUS FAVORITOS" else "EXPLORA · NASA APOD", color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.labelSmall, letterSpacing = 2.sp)
                    Text(if (favoritesOnly) "Mis imágenes\nfavoritas" else "Tu ventana\nal universo.", fontSize = 36.sp, lineHeight = 41.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-1).sp)
                    Text(if (favoritesOnly) "Los descubrimientos que elegiste guardar." else "Siete días de imágenes e historias para mirar más allá.", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        LabelPill(if (favoritesOnly) "${state.images.size} guardadas" else "Últimos 7 días")
                        state.range?.let { Text("${formatDate(it.start.toString(), "d MMM")} — ${formatDate(it.end.toString(), "d MMM")}", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant) }
                    }
                }
            }
            actionError?.let { message -> item { MessageBanner(message, onDismiss = onDismissError) } }
            state.message?.let { message -> item { MessageBanner(message, onRetry = if (state.canRetry && !state.isLoading) onRefresh else null) } }
            if (state.isLoading && state.images.isNotEmpty()) item { LinearProgressIndicator(Modifier.fillMaxWidth().clip(CircleShape)) }
            when {
                state.isLoading && state.images.isEmpty() -> item { LoadingContent() }
                state.images.isEmpty() -> item { EmptyContent(
                    if (favoritesOnly) "Todavía no hay favoritos. Toca el corazón de una imagen para guardarla aquí."
                    else "No hay imágenes disponibles para estas fechas. Puedes volver a consultar NASA.",
                    if (!favoritesOnly) onRefresh else null,
                ) }
                else -> {
                    item {
                        Row(Modifier.fillMaxWidth().padding(top = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                            Text(if (favoritesOnly) "Tu colección" else "Imágenes de la semana", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            Text("${state.images.size} descubrimientos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                    }
                    itemsIndexed(state.images, key = { _, image -> image.date }) { index, image ->
                        ApodCard(image, featured = index == 0, onOpen = { onOpenDetail(image.date) }, onToggleFavorite = { onToggleFavorite(image.date) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ApodCard(image: ApodEntity, featured: Boolean, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable(onClickLabel = "Ver detalle de ${image.title}", onClick = onOpen),
        shape = RoundedCornerShape(24.dp), color = MaterialTheme.colorScheme.surfaceContainer,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.65f)),
    ) {
        if (featured) Column {
            Box {
                ApodMedia(image, large = false, modifier = Modifier.fillMaxWidth().aspectRatio(1.35f))
                Box(Modifier.matchParentSize().background(Brush.verticalGradient(listOf(Color.Transparent, Color(0xE6080D19)), startY = 150f)))
                Row(Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                    Surface(shape = CircleShape, color = Color(0xC9121D2E)) {
                        Text("MÁS RECIENTE", Modifier.padding(horizontal = 12.dp, vertical = 8.dp), color = Color.White, style = MaterialTheme.typography.labelSmall, letterSpacing = 1.sp)
                    }
                    Spacer(Modifier.weight(1f))
                    FavoriteButton(image, onToggleFavorite)
                }
                Text(image.title, modifier = Modifier.align(Alignment.BottomStart).padding(20.dp), color = Color.White, fontSize = 25.sp, lineHeight = 30.sp, fontWeight = FontWeight.SemiBold)
            }
            Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 15.dp), verticalAlignment = Alignment.CenterVertically) {
                SpaceIcon(SpaceSymbol.Calendar, Modifier.size(16.dp), MaterialTheme.colorScheme.onSurfaceVariant)
                Text(formatDate(image.date), Modifier.padding(start = 8.dp).weight(1f), style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text("Explorar", style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
                SpaceIcon(SpaceSymbol.Arrow, Modifier.padding(start = 8.dp).size(18.dp), MaterialTheme.colorScheme.primary)
            }
        } else Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            ApodMedia(image, large = false, modifier = Modifier.size(96.dp).clip(RoundedCornerShape(16.dp)))
            Column(Modifier.weight(1f).padding(start = 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(formatDate(image.date), style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.secondary)
                Text(image.title, style = MaterialTheme.typography.titleSmall, maxLines = 3, overflow = TextOverflow.Ellipsis)
                if (image.mediaType == "video") Text("Contenido en video", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            FavoriteButton(image, onToggleFavorite)
        }
    }
}

@Composable
private fun ApodDetailScreen(
    state: ApodDetailState,
    actionError: String?,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val image = state.image
    Column(Modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            RoundAction(SpaceSymbol.Back, "Volver", onBack)
            Text("EXPLORACIÓN APOD", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, letterSpacing = 2.sp)
            if (image != null) FavoriteButton(image) { onToggleFavorite(image.date) } else Spacer(Modifier.width(44.dp))
        }
        actionError?.let { MessageBanner(it, onDismiss = onDismissError) }
        when {
            state.isLoading -> LoadingContent()
            image == null -> EmptyContent(state.error ?: "Esta imagen no está disponible en la base local. Actualiza la lista para volver a consultar NASA.")
            else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                Box(Modifier.padding(horizontal = 16.dp).clip(RoundedCornerShape(28.dp))) {
                    ApodMedia(image, large = true, modifier = Modifier.fillMaxWidth().aspectRatio(1f))
                }
                Column(Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    LabelPill(if (image.mediaType == "video") "CONTENIDO EN VIDEO" else "IMAGEN ASTRONÓMICA")
                    Text(image.title, fontSize = 30.sp, lineHeight = 36.sp, fontWeight = FontWeight.SemiBold, letterSpacing = (-0.5).sp)
                    Text(formatDate(image.date, "d 'de' MMMM 'de' yyyy"), color = MaterialTheme.colorScheme.secondary, style = MaterialTheme.typography.bodyMedium)
                    image.copyright?.takeIf { it.isNotBlank() }?.let {
                        Text("Créditos · $it", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
                    Text("La historia detrás de la imagen", style = MaterialTheme.typography.titleMedium)
                    Text(image.explanation, fontSize = 16.sp, lineHeight = 27.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.90f))
                    Text("ASTRONOMY PICTURE OF THE DAY · NASA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
private fun ApodMedia(image: ApodEntity, large: Boolean, modifier: Modifier) {
    val background = modifier.background(MaterialTheme.colorScheme.surfaceContainerHigh)
    val url = if (large) image.hdUrl ?: image.url else image.url ?: image.hdUrl
    when {
        image.mediaType == "video" -> MediaMessage("Contenido en video", background, SpaceSymbol.Play)
        image.mediaType != "image" || url == null -> MediaMessage("Imagen no disponible", background, SpaceSymbol.Image)
        else -> SubcomposeAsyncImage(
            model = url, contentDescription = image.title, modifier = background,
            contentScale = if (large) ContentScale.Fit else ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator(Modifier.size(28.dp), strokeWidth = 2.dp) } },
            error = { MediaMessage("No se pudo cargar la imagen", Modifier.fillMaxSize(), SpaceSymbol.Image) },
        )
    }
}

@Composable
private fun MediaMessage(text: String, modifier: Modifier, symbol: SpaceSymbol) {
    Column(modifier.padding(12.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        SpaceIcon(symbol, Modifier.size(28.dp), MaterialTheme.colorScheme.primary)
        Spacer(Modifier.height(10.dp))
        Text(text, style = MaterialTheme.typography.labelSmall, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun FavoriteButton(image: ApodEntity, onToggle: () -> Unit) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainerHigh.copy(alpha = 0.93f)) {
        IconToggleButton(checked = image.isFavorite, onCheckedChange = { onToggle() }, modifier = Modifier.size(44.dp).semantics {
            contentDescription = if (image.isFavorite) "Quitar de favoritos: ${image.title}" else "Agregar a favoritos: ${image.title}"
        }) {
            SpaceIcon(SpaceSymbol.Heart, Modifier.size(21.dp), if (image.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface, filled = image.isFavorite)
        }
    }
}

@Composable
private fun RoundAction(symbol: SpaceSymbol, label: String, onClick: () -> Unit, enabled: Boolean = true) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.surfaceContainer) {
        IconButton(onClick = onClick, enabled = enabled, modifier = Modifier.size(44.dp).semantics { contentDescription = label }) {
            SpaceIcon(symbol, Modifier.size(21.dp), if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f))
        }
    }
}

@Composable
private fun LabelPill(text: String) {
    Surface(shape = CircleShape, color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.55f)) {
        Text(text, Modifier.padding(horizontal = 12.dp, vertical = 7.dp), color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelSmall)
    }
}

@Composable
private fun MessageBanner(message: String, onRetry: (() -> Unit)? = null, onDismiss: (() -> Unit)? = null) {
    Surface(Modifier.fillMaxWidth(), color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.50f), shape = RoundedCornerShape(18.dp), border = BorderStroke(1.dp, MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f))) {
        Column(Modifier.padding(16.dp)) {
            Text(message, style = MaterialTheme.typography.bodySmall, lineHeight = 19.sp, color = MaterialTheme.colorScheme.onSecondaryContainer)
            onRetry?.let { TextButton(onClick = it) { Text("Volver a intentar") } }
            onDismiss?.let { TextButton(onClick = it) { Text("Cerrar mensaje") } }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(Modifier.fillMaxWidth().padding(vertical = 56.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator(Modifier.size(32.dp), strokeWidth = 2.dp)
        Spacer(Modifier.height(18.dp))
        Text("Buscando nuevos descubrimientos…", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@Composable
private fun EmptyContent(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxWidth().padding(vertical = 40.dp, horizontal = 12.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(18.dp)) {
        SpaceIcon(SpaceSymbol.Orbit, Modifier.size(52.dp), MaterialTheme.colorScheme.primary)
        Text(message, textAlign = TextAlign.Center, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
        onRetry?.let { FilledTonalButton(onClick = it) { Text("Actualizar imágenes") } }
    }
}

private fun formatDate(date: String, pattern: String = "d MMM yyyy"): String = runCatching {
    LocalDate.parse(date).format(DateTimeFormatter.ofPattern(pattern, Locale.forLanguageTag("es-PE")))
}.getOrDefault(date)
