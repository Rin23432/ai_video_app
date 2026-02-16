package com.animegen.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import com.animegen.app.AppContainer
import com.animegen.app.R
import com.animegen.app.data.network.CommunityRankingAuthorItem
import com.animegen.app.data.network.CommunityRankingContentItem
import com.animegen.app.data.network.CommunityRankingTagItem
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val TYPE_CONTENT = "content"
private const val TYPE_AUTHOR = "author"
private const val TYPE_TAG = "tag"

data class CommunityRankingUiState(
    val loading: Boolean = false,
    val rankType: String = TYPE_CONTENT,
    val window: String = "weekly",
    val contentItems: List<CommunityRankingContentItem> = emptyList(),
    val authorItems: List<CommunityRankingAuthorItem> = emptyList(),
    val tagItems: List<CommunityRankingTagItem> = emptyList(),
    val errorMessage: String? = null
)

class CommunityRankingViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityRankingUiState())
    val uiState: StateFlow<CommunityRankingUiState> = _uiState.asStateFlow()

    fun switchType(type: String) {
        _uiState.update { it.copy(rankType = type) }
        load()
    }

    fun switchWindow(window: String) {
        _uiState.update { it.copy(window = window) }
        load()
    }

    fun load() {
        val state = _uiState.value
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (state.rankType) {
                TYPE_CONTENT -> when (val result = repository.contentRankings(state.window)) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(loading = false, contentItems = result.data.items)
                    }
                    is AppResult.Failure -> _uiState.update {
                        it.copy(loading = false, errorMessage = result.error.displayMessage)
                    }
                }
                TYPE_AUTHOR -> when (val result = repository.authorRankings(state.window)) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(loading = false, authorItems = result.data.items)
                    }
                    is AppResult.Failure -> _uiState.update {
                        it.copy(loading = false, errorMessage = result.error.displayMessage)
                    }
                }
                else -> when (val result = repository.tagRankings(state.window)) {
                    is AppResult.Success -> _uiState.update {
                        it.copy(loading = false, tagItems = result.data.items)
                    }
                    is AppResult.Failure -> _uiState.update {
                        it.copy(loading = false, errorMessage = result.error.displayMessage)
                    }
                }
            }
        }
    }
}

@Composable
fun CommunityRankingRoute(
    container: AppContainer,
    onOpenContent: (Long) -> Unit,
    onOpenTag: (Long) -> Unit
) {
    val vm: CommunityRankingViewModel = viewModel(factory = viewModelFactory {
        CommunityRankingViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.load() }
    CommunityRankingScreen(
        state = state,
        onSwitchType = vm::switchType,
        onSwitchWindow = vm::switchWindow,
        onRetry = vm::load,
        onOpenContent = onOpenContent,
        onOpenTag = onOpenTag
    )
}

@Composable
private fun CommunityRankingScreen(
    state: CommunityRankingUiState,
    onSwitchType: (String) -> Unit,
    onSwitchWindow: (String) -> Unit,
    onRetry: () -> Unit,
    onOpenContent: (Long) -> Unit,
    onOpenTag: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.rankType == TYPE_CONTENT, onClick = { onSwitchType(TYPE_CONTENT) }, label = { Text(stringResource(R.string.ranking_type_content)) })
            FilterChip(selected = state.rankType == TYPE_AUTHOR, onClick = { onSwitchType(TYPE_AUTHOR) }, label = { Text(stringResource(R.string.ranking_type_author)) })
            FilterChip(selected = state.rankType == TYPE_TAG, onClick = { onSwitchType(TYPE_TAG) }, label = { Text(stringResource(R.string.ranking_type_tag)) })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            FilterChip(selected = state.window == "daily", onClick = { onSwitchWindow("daily") }, label = { Text(stringResource(R.string.ranking_window_daily)) })
            FilterChip(selected = state.window == "weekly", onClick = { onSwitchWindow("weekly") }, label = { Text(stringResource(R.string.ranking_window_weekly)) })
            FilterChip(selected = state.window == "monthly", onClick = { onSwitchWindow("monthly") }, label = { Text(stringResource(R.string.ranking_window_monthly)) })
        }

        if (state.loading) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = onRetry)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            if (state.rankType == TYPE_CONTENT) {
                items(state.contentItems, key = { "c-${it.contentId}" }) { row ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenContent(row.contentId) }) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "#${row.rank} ${row.title}", style = MaterialTheme.typography.titleMedium)
                            Text(text = stringResource(R.string.ranking_content_meta, row.author.nickname, row.score, row.deltaScore))
                        }
                    }
                }
            } else if (state.rankType == TYPE_AUTHOR) {
                items(state.authorItems, key = { "a-${it.author.userId}" }) { row ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "#${row.rank} ${row.author.nickname}", style = MaterialTheme.typography.titleMedium)
                            Text(text = stringResource(R.string.ranking_author_meta, row.publishedCount, row.likesReceived, row.score))
                        }
                    }
                }
            } else {
                items(state.tagItems, key = { "t-${it.tagId}" }) { row ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenTag(row.tagId) }) {
                        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text(text = "#${row.rank} #${row.name}", style = MaterialTheme.typography.titleMedium)
                            Text(text = stringResource(R.string.ranking_tag_meta, row.contentCount, row.score, row.deltaScore))
                        }
                    }
                }
            }
        }
    }
}
