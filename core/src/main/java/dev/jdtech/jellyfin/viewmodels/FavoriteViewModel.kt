package dev.jdtech.jellyfin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dev.jdtech.jellyfin.Constants
import dev.jdtech.jellyfin.core.R
import dev.jdtech.jellyfin.models.FavoriteSection
import dev.jdtech.jellyfin.models.FindroidEpisode
import dev.jdtech.jellyfin.models.FindroidMovie
import dev.jdtech.jellyfin.models.FindroidShow
import dev.jdtech.jellyfin.models.UiText
import dev.jdtech.jellyfin.repository.JellyfinRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.jellyfin.sdk.model.api.BaseItemKind
import javax.inject.Inject

@HiltViewModel
class FavoriteViewModel
@Inject
constructor(
    private val jellyfinRepository: JellyfinRepository,
) : ViewModel() {
    private val _uiState = MutableStateFlow<UiState>(UiState.Loading)
    val uiState = _uiState.asStateFlow()

    sealed class UiState {
        data class Normal(val favoriteSections: List<FavoriteSection>) : UiState()
        data object Loading : UiState()
        data class Error(val error: Exception) : UiState()
    }

    init {
        loadData()
    }

    fun loadData() {
        viewModelScope.launch {
            _uiState.emit(UiState.Loading)
            try {
                val movieItems = jellyfinRepository.getFavoriteItems(BaseItemKind.MOVIE, null)
                val episodeItems = jellyfinRepository.getFavoriteItems(BaseItemKind.EPISODE, 20)
                val seriesItems = jellyfinRepository.getFavoriteItems(BaseItemKind.SERIES, 20)
                val favoriteSections = mutableListOf<FavoriteSection>()
                withContext(Dispatchers.Default) {
                    if (movieItems.isNotEmpty()) {
                        favoriteSections.add(
                            FavoriteSection(
                                Constants.FAVORITE_TYPE_MOVIES,
                                UiText.StringResource(R.string.movies_label),
                                movieItems,
                            ),
                        )
                    }

                    if (seriesItems.isNotEmpty()) {
                        favoriteSections.add(
                            FavoriteSection(
                                Constants.FAVORITE_TYPE_SHOWS,
                                UiText.StringResource(R.string.shows_label),
                                seriesItems,
                            ),
                        )
                    }

                    if (episodeItems.isNotEmpty()) {
                        favoriteSections.add(
                            FavoriteSection(
                                Constants.FAVORITE_TYPE_EPISODES,
                                UiText.StringResource(R.string.episodes_label),
                                episodeItems,
                            ),
                        )
                    }
                }
                _uiState.emit(UiState.Normal(favoriteSections))
            } catch (e: Exception) {
                _uiState.emit(UiState.Error(e))
            }
        }
    }
}
