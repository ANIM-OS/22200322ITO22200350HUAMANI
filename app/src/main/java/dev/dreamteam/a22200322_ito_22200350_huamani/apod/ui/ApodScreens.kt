package dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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

@OptIn(ExperimentalMaterial3Api::class)
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
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text(if (favoritesOnly) "Mis imágenes favoritas" else "Imágenes", maxLines = 1, overflow = TextOverflow.Ellipsis) },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = { if (favoritesOnly) BackButton(onBack) },
                actions = {
                    if (!favoritesOnly) {
                        TextButton(onClick = onOpenFavorites) { Text("Favoritas") }
                        IconButton(
                            onClick = onRefresh,
                            enabled = !state.isLoading,
                            modifier = Modifier.semantics { contentDescription = "Actualizar imágenes" },
                        ) { Text("↻", fontSize = 26.sp) }
                    }
                },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            state.range?.let {
                Text(
                    "Últimos 7 días · ${it.start} al ${it.end}",
                    style = MaterialTheme.typography.labelMedium,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                )
            }
            actionError?.let { MessageBanner(it, onDismiss = onDismissError) }
            state.message?.let {
                MessageBanner(it, onRetry = if (state.canRetry && !state.isLoading) onRefresh else null)
            }
            if (state.isLoading && state.images.isNotEmpty()) LinearProgressIndicator(Modifier.fillMaxWidth())
            when {
                state.isLoading && state.images.isEmpty() -> LoadingContent()
                state.images.isEmpty() -> EmptyContent(
                    if (favoritesOnly) "Aún no tienes imágenes favoritas. Marca una estrella en la lista o en el detalle."
                    else "No hay imágenes guardadas para estos días.",
                    onRetry = if (!favoritesOnly) onRefresh else null,
                )
                else -> LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    items(state.images, key = { it.date }) { image ->
                        ApodCard(image, { onOpenDetail(image.date) }, { onToggleFavorite(image.date) })
                    }
                }
            }
        }
    }
}

@Composable
private fun ApodCard(image: ApodEntity, onOpen: () -> Unit, onToggleFavorite: () -> Unit) {
    Card(Modifier.fillMaxWidth().clickable(onClickLabel = "Ver detalle de ${image.title}", onClick = onOpen)) {
        ApodMedia(image, large = false)
        Row(Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(image.title, style = MaterialTheme.typography.titleMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
                Spacer(Modifier.height(4.dp))
                Text(image.date, style = MaterialTheme.typography.bodySmall)
            }
            FavoriteButton(image, onToggleFavorite)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ApodDetailScreen(
    state: ApodDetailState,
    actionError: String?,
    onDismissError: () -> Unit,
    onBack: () -> Unit,
    onToggleFavorite: (String) -> Unit,
) {
    val image = state.image
    Scaffold(
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = { Text("Detalle APOD") },
                windowInsets = WindowInsets(0, 0, 0, 0),
                navigationIcon = { BackButton(onBack) },
                actions = { image?.let { FavoriteButton(it) { onToggleFavorite(it.date) } } },
            )
        },
    ) { padding ->
        Column(Modifier.fillMaxSize().padding(padding)) {
            actionError?.let { MessageBanner(it, onDismiss = onDismissError) }
            when {
                state.isLoading -> LoadingContent()
                image == null -> EmptyContent(state.error ?: "Esta imagen no está disponible en la base local. Vuelve a la lista para actualizar.")
                else -> Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
                    ApodMedia(image, large = true)
                    Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Text(image.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(image.date, style = MaterialTheme.typography.labelLarge)
                        image.copyright?.takeIf { it.isNotBlank() }?.let {
                            Text("Autor: $it", style = MaterialTheme.typography.bodyMedium)
                        }
                        Text(image.explanation, style = MaterialTheme.typography.bodyLarge)
                    }
                }
            }
        }
    }
}

@Composable
private fun ApodMedia(image: ApodEntity, large: Boolean) {
    val modifier = Modifier.fillMaxWidth().aspectRatio(if (large) 1f else 16f / 9f)
        .background(MaterialTheme.colorScheme.surfaceContainerHighest)
    val url = if (large) image.hdUrl ?: image.url else image.url ?: image.hdUrl
    // Video y tipos desconocidos nunca llegan al cargador de imágenes.
    when {
        image.mediaType == "video" -> MediaMessage("Contenido en video", modifier)
        image.mediaType != "image" -> MediaMessage("Contenido no disponible", modifier)
        url == null -> MediaMessage("Imagen no disponible", modifier)
        else -> SubcomposeAsyncImage(
            model = url,
            contentDescription = image.title,
            modifier = modifier,
            contentScale = if (large) ContentScale.Fit else ContentScale.Crop,
            loading = { Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() } },
            error = { MediaMessage("No se pudo cargar la imagen.", Modifier.fillMaxSize()) },
        )
    }
}

@Composable
private fun MediaMessage(text: String, modifier: Modifier) {
    Box(modifier, contentAlignment = Alignment.Center) {
        Text(text, Modifier.padding(16.dp), textAlign = TextAlign.Center)
    }
}

@Composable
private fun FavoriteButton(image: ApodEntity, onToggle: () -> Unit) {
    IconToggleButton(
        checked = image.isFavorite,
        onCheckedChange = { onToggle() },
        modifier = Modifier.semantics {
            contentDescription = if (image.isFavorite) "Quitar de favoritos: ${image.title}" else "Agregar a favoritos: ${image.title}"
        },
    ) {
        Text(
            if (image.isFavorite) "★" else "☆",
            color = if (image.isFavorite) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 30.sp,
        )
    }
}

@Composable
private fun BackButton(onBack: () -> Unit) {
    IconButton(onClick = onBack, modifier = Modifier.semantics { contentDescription = "Volver" }) {
        Text("←", fontSize = 26.sp)
    }
}

@Composable
private fun MessageBanner(message: String, onRetry: (() -> Unit)? = null, onDismiss: (() -> Unit)? = null) {
    Surface(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp), color = MaterialTheme.colorScheme.secondaryContainer, shape = MaterialTheme.shapes.medium) {
        Column(Modifier.padding(12.dp)) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            onRetry?.let { TextButton(onClick = it) { Text("Reintentar") } }
            onDismiss?.let { TextButton(onClick = it) { Text("Cerrar mensaje") } }
        }
    }
}

@Composable
private fun LoadingContent() {
    Column(Modifier.fillMaxSize(), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        CircularProgressIndicator()
        Spacer(Modifier.height(12.dp))
        Text("Cargando imágenes…")
    }
}

@Composable
private fun EmptyContent(message: String, onRetry: (() -> Unit)? = null) {
    Column(Modifier.fillMaxSize().padding(24.dp), verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(message, textAlign = TextAlign.Center)
        onRetry?.let {
            Spacer(Modifier.height(12.dp))
            Button(onClick = it) { Text("Actualizar") }
        }
    }
}
