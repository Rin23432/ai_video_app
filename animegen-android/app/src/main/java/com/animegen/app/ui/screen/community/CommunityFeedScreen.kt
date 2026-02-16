package com.animegen.app.ui.screen.community

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animegen.app.AppContainer
import com.animegen.app.R
import com.animegen.app.data.network.CommunityContentSummary
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityFeedUiState(
    val tab: String = "latest",
    val loading: Boolean = false,
    val items: List<CommunityContentSummary> = emptyList(),
    val showingCachedData: Boolean = false,
    val errorMessage: String? = null
)

class CommunityFeedViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState: StateFlow<CommunityFeedUiState> = _uiState.asStateFlow()

    fun switchTab(tab: String) {
        if (tab == "same") {
            _uiState.update { it.copy(tab = tab, loading = false, showingCachedData = false, errorMessage = null) }
            return
        }
        load(tab)
    }

    private fun load(tab: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, tab = tab) }
            val cached = repository.feedCached(tab)
            if (cached.isNotEmpty()) {
                _uiState.update {
                    it.copy(items = cached, showingCachedData = true)
                }
            } else {
                _uiState.update { it.copy(showingCachedData = false) }
            }
            when (val result = repository.feed(tab = tab)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(
                        loading = false,
                        items = result.data.items,
                        showingCachedData = false
                    )
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(
                        loading = false,
                        errorMessage = result.error.displayMessage,
                        showingCachedData = cached.isNotEmpty()
                    )
                }
            }
        }
    }
}

@Composable
fun CommunityFeedRoute(
    container: AppContainer,
    onOpenDetail: (Long) -> Unit,
    onOpenFavorites: () -> Unit,
    onOpenPublished: () -> Unit,
    onOpenTagDetail: (Long) -> Unit
) {
    val vm: CommunityFeedViewModel = viewModel(factory = viewModelFactory {
        CommunityFeedViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.switchTab("latest") }

    val listState = rememberLazyListState()
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = stringResource(R.string.community_feed_title),
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = stringResource(R.string.community_feed_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FeedTabChip(selected = state.tab == "latest", label = stringResource(R.string.community_tab_latest)) { vm.switchTab("latest") }
            FeedTabChip(selected = state.tab == "hot", label = stringResource(R.string.community_tab_hot)) { vm.switchTab("hot") }
            FeedTabChip(selected = state.tab == "same", label = stringResource(R.string.community_tab_tags)) { vm.switchTab("same") }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedButton(onClick = onOpenFavorites, shape = RoundedCornerShape(22.dp)) {
                Text(stringResource(R.string.community_favorites))
            }
            OutlinedButton(onClick = onOpenPublished, shape = RoundedCornerShape(22.dp)) {
                Text(stringResource(R.string.community_published))
            }
        }

        if (state.tab == "same") {
            TagHubRoute(container = container, onOpenTag = onOpenTagDetail)
            return@Column
        }

        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            ErrorNotice(message = errorMessage, onRetry = { vm.switchTab(state.tab) })
        }

        if (state.showingCachedData) {
            Text(
                text = stringResource(R.string.community_feed_cached_hint),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        if (state.loading) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 32.dp),
                horizontalArrangement = Arrangement.Center
            ) {
                CircularProgressIndicator()
            }
        }

        LazyColumn(
            state = listState,
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            items(state.items, key = { it.contentId }) { item ->
                CommunityStoryCard(item = item, onOpenDetail = onOpenDetail)
            }
        }
    }
}

@Composable
private fun FeedTabChip(
    selected: Boolean,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = selected,
        onClick = onClick,
        label = { Text(label) },
        shape = RoundedCornerShape(18.dp),
        colors = FilterChipDefaults.filterChipColors(
            containerColor = Color.White,
            selectedContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            selectedLabelColor = MaterialTheme.colorScheme.onSurface
        )

        )
}

@Composable
private fun CommunityStoryCard(
    item: CommunityContentSummary,
    onOpenDetail: (Long) -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenDetail(item.contentId) },
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AsyncImage(
                model = item.coverUrl,
                contentDescription = item.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(176.dp)
                    .clip(RoundedCornerShape(16.dp)),
                contentScale = ContentScale.Crop
            )

            Text(
                text = item.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = stringResource(R.string.community_author_format, item.author.nickname),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatPill(label = stringResource(R.string.community_stat_like), value = item.likeCount)
                StatPill(label = stringResource(R.string.community_stat_favorite), value = item.favoriteCount)
                StatPill(label = stringResource(R.string.community_stat_comment), value = item.commentCount)
            }
        }
    }
}

@Composable
private fun StatPill(label: String, value: Int) {
    Card(
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Text(
            text = stringResource(R.string.community_stat_pill_format, label, value),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}





