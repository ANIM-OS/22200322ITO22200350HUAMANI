package dev.dreamteam.a22200322_ito_22200350_huamani.apod.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonParseException
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.*
import dev.dreamteam.a22200322_ito_22200350_huamani.apod.data.local.ApodEntity
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

data class ApodListState(
    val images: List<ApodEntity> = emptyList(),
    val isLoading: Boolean = true,
    val message: String? = null,
    val canRetry: Boolean = false,
    val range: ApodDateRange? = null,
)

data class ApodDetailState(
    val image: ApodEntity? = null,
    val isLoading: Boolean = true,
    val error: String? = null,
)

private data class RefreshState(
    val loading: Boolean = false,
    val message: String? = null,
    val failed: Boolean = false,
)

@OptIn(ExperimentalCoroutinesApi::class)
class ApodViewModel(private val repository: ApodRepository) : ViewModel() {
    private val range = MutableStateFlow(repository.currentRange())
    private val refreshState = MutableStateFlow(RefreshState())
    private val mutableActionError = MutableStateFlow<String?>(null)
    val actionError: StateFlow<String?> = mutableActionError.asStateFlow()
    private var refreshJob: Job? = null

    val images: StateFlow<ApodListState> = range.flatMapLatest { dates ->
        combine(repository.observeImages(dates), refreshState) { items, refresh ->
            ApodListState(items, refresh.loading, refresh.message, refresh.failed, dates)
        }
    }.catch {
        if (it is CancellationException) throw it
        emit(ApodListState(isLoading = false, message = "No se pudo leer la base local. Vuelve a abrir Imágenes.", range = range.value))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApodListState())

    val favorites: StateFlow<ApodListState> = repository.observeFavorites()
        .map { ApodListState(images = it, isLoading = false) }
        .catch {
            if (it is CancellationException) throw it
            emit(ApodListState(isLoading = false, message = "No se pudieron leer tus favoritos."))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApodListState())

    fun detail(date: String): StateFlow<ApodDetailState> = repository.observeDetail(date)
        .map { ApodDetailState(image = it, isLoading = false) }
        .catch {
            if (it is CancellationException) throw it
            emit(ApodDetailState(isLoading = false, error = "No se pudo leer esta imagen de la base local."))
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ApodDetailState())

    fun refresh() {
        if (refreshJob?.isActive == true) return
        refreshJob = viewModelScope.launch {
            refreshState.value = RefreshState(loading = true)
            val dates = repository.currentRange()
            range.value = dates
            try {
                val result = repository.synchronize(dates)
                val message = when {
                    result.saved == 0 -> "NASA no devolvió datos válidos para estos días. Se conservan los datos locales disponibles."
                    result.skipped > 0 -> "Se omitieron registros incompletos o repetidos; los datos válidos ya están guardados."
                    else -> null
                }
                refreshState.value = RefreshState(message = message)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                // Nunca exponer e.message: puede incluir la URL de consulta con la clave.
                refreshState.value = RefreshState(message = userMessage(e), failed = true)
            }
        }
    }

    fun toggleFavorite(date: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(date)
            } catch (e: CancellationException) {
                throw e
            } catch (_: Exception) {
                mutableActionError.value = "No se pudo cambiar el favorito. Inténtalo de nuevo."
            }
        }
    }

    fun dismissActionError() { mutableActionError.value = null }

    class Factory(private val repository: ApodRepository) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(ApodViewModel::class.java))
            @Suppress("UNCHECKED_CAST")
            return ApodViewModel(repository) as T
        }
    }
}

private fun userMessage(error: Exception): String = when (error) {
    is MissingNasaKeyException -> "Configura NASA_API_KEY en local.properties y vuelve a compilar la app."
    is HttpException -> when (error.code()) {
        401, 403 -> "NASA rechazó la clave o el acceso. Revisa tu clave personal."
        429 -> "Se alcanzó el límite de consultas de NASA. Inténtalo más tarde."
        else -> "NASA no pudo responder. Puedes reintentar; tus datos locales se conservan."
    }
    is IOException -> "No se pudo conectar con NASA. Revisa tu conexión; tus datos locales se conservan."
    is JsonParseException, is IncompleteApodResponseException -> "NASA devolvió una respuesta incompleta. Inténtalo de nuevo."
    else -> "No se pudieron actualizar las imágenes. Los datos guardados siguen disponibles."
}
