package dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.AsteroidRepository
import dev.dreamteam.a22200322_ito_22200350_huamani.asteroides.data.local.AsteroidEntity
import java.time.LocalDate
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

data class AsteroidUiState(
    val asteroids: List<AsteroidEntity> = emptyList(),
    val favorites: List<AsteroidEntity> = emptyList(),
    val onlyHazardous: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
)

data class AsteroidDetailState(
    val asteroid: AsteroidEntity? = null,
    val isLoading: Boolean = true,
)

class AsteroidViewModel(private val repository: AsteroidRepository) : ViewModel() {
    private val today = LocalDate.now()
    private val onlyHazardous = MutableStateFlow(false)
    private val isLoading = MutableStateFlow(false)
    private val errorMessage = MutableStateFlow<String?>(null)
    private var syncJob: Job? = null

    val uiState: StateFlow<AsteroidUiState> = combine(
        repository.observeRecent(today.minusDays(6), today),
        repository.observeFavorites(),
        onlyHazardous,
        isLoading,
        errorMessage,
    ) { recent, favorites, hazardousOnly, loading, error ->
        AsteroidUiState(
            asteroids = if (hazardousOnly) recent.filter { it.isPotentiallyHazardous == true } else recent,
            favorites = favorites,
            onlyHazardous = hazardousOnly,
            isLoading = loading,
            errorMessage = error,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AsteroidUiState(isLoading = true))

    init {
        refresh()
    }

    fun setOnlyHazardous(enabled: Boolean) {
        onlyHazardous.value = enabled
    }

    fun refresh() {
        if (syncJob?.isActive == true) return
        syncJob = viewModelScope.launch {
            isLoading.value = true
            errorMessage.value = null
            try {
                repository.syncRecent(today)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorMessage.value = error.message ?: "No se pudo actualizar Asteroides."
            } finally {
                isLoading.value = false
            }
        }
    }

    fun toggleFavorite(id: String) {
        viewModelScope.launch {
            try {
                repository.toggleFavorite(id)
            } catch (error: CancellationException) {
                throw error
            } catch (error: Exception) {
                errorMessage.value = "No se pudo guardar el favorito."
            }
        }
    }

    /** Each detail screen subscribes to Room for its own ID. */
    fun observeDetail(id: String): StateFlow<AsteroidDetailState> =
        repository.observeById(id)
            .map { AsteroidDetailState(asteroid = it, isLoading = false) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AsteroidDetailState())

    class Factory(private val repository: AsteroidRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            require(modelClass.isAssignableFrom(AsteroidViewModel::class.java))
            return AsteroidViewModel(repository) as T
        }
    }
}
