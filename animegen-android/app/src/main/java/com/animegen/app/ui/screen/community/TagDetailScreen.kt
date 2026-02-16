package com.animegen.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animegen.app.AppContainer
import com.animegen.app.R
import com.animegen.app.data.network.CommunityContentSummary
import com.animegen.app.data.network.CommunityTagDetail
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TagDetailUiState(
    val loading: Boolean = false,
    val tab: String = "latest",
    val detail: CommunityTagDetail? = null,
    val items: List<CommunityContentSummary> = emptyList(),
    val errorMessage: String? = null
)

class TagDetailViewModel(private val repository: CommunityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TagDetailUiState())
    val uiState: StateFlow<TagDetailUiState> = _uiState.asStateFlow()

    fun load(tagId: Long, tab: String = _uiState.value.tab) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, tab = tab) }
            val detailResult = repository.tagDetail(tagId)
            val feedResult = repository.tagContents(tagId, tab)
            if (detailResult is AppResult.Success && feedResult is AppResult.Success) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        detail = detailResult.data,
                        items = feedResult.data.items
                    )
                }
            } else {
                val message = when {
                    detailResult is AppResult.Failure -> detailResult.error.displayMessage
                    feedResult is AppResult.Failure -> feedResult.error.displayMessage
                    else -> "加载失败"
                }
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
        }
    }
}

@Composable
fun TagDetailRoute(container: AppContainer, tagIdArg: String?, onOpenContent: (Long) -> Unit) {
    val vm: TagDetailViewModel = viewModel(factory = viewModelFactory {
        TagDetailViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()
    val tagId = tagIdArg?.toLongOrNull()

    LaunchedEffect(tagId) {
        if (tagId != null) {
            vm.load(tagId)
        }
    }

    Column(
        modifier = Modifier.padding(12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        if (state.loading) CircularProgressIndicator()
        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            ErrorNotice(message = errorMessage, onRetry = { if (tagId != null) vm.load(tagId, state.tab) })
        }
        val detail = state.detail
        if (detail != null) {
            Text("#${detail.name}", style = MaterialTheme.typography.titleLarge)
            Text(stringResource(R.string.tag_meta_format, detail.contentCount, detail.hotScore))
            if (!detail.description.isNullOrBlank()) {
                Text(detail.description)
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(onClick = { if (tagId != null) vm.load(tagId, "latest") }, enabled = state.tab != "latest") {
                    Text(stringResource(R.string.community_tab_latest))
                }
                Button(onClick = { if (tagId != null) vm.load(tagId, "hot") }, enabled = state.tab != "hot") {
                    Text(stringResource(R.string.community_tab_hot))
                }
            }
            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(state.items, key = { it.contentId }) { item ->
                    Card(modifier = Modifier.fillMaxWidth().clickable { onOpenContent(item.contentId) }) {
                        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                            AsyncImage(
                                model = item.coverUrl,
                                contentDescription = item.title,
                                modifier = Modifier.width(120.dp).height(80.dp),
                                contentScale = ContentScale.Crop
                            )
                            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                                Text(item.title, style = MaterialTheme.typography.titleMedium)
                                Text(stringResource(R.string.community_author_format, item.author.nickname))
                                Text(stringResource(R.string.tag_detail_stats_format, item.likeCount, item.favoriteCount, item.commentCount))
                            }
                        }
                    }
                }
            }
        }
    }
}






