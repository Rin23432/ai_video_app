package com.animegen.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import com.animegen.app.data.network.CommunityTag
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TagHubUiState(
    val loading: Boolean = false,
    val searching: Boolean = false,
    val keyword: String = "",
    val hotTags: List<CommunityTag> = emptyList(),
    val searchTags: List<CommunityTag> = emptyList(),
    val errorMessage: String? = null
)

class TagHubViewModel(private val repository: CommunityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(TagHubUiState())
    val uiState: StateFlow<TagHubUiState> = _uiState.asStateFlow()

    fun loadHot() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.hotTags()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(loading = false, hotTags = result.data.items)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun setKeyword(value: String) {
        _uiState.update { it.copy(keyword = value) }
    }

    fun search() {
        val keyword = _uiState.value.keyword.trim()
        if (keyword.isBlank()) {
            _uiState.update { it.copy(searchTags = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(searching = true, errorMessage = null) }
            when (val result = repository.searchTags(keyword)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(searching = false, searchTags = result.data.items)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(searching = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun TagHubRoute(container: AppContainer, onOpenTag: (Long) -> Unit) {
    val vm: TagHubViewModel = viewModel(factory = viewModelFactory {
        TagHubViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.loadHot() }

    TagHubScreen(
        state = state,
        onKeywordChange = vm::setKeyword,
        onSearch = vm::search,
        onRetry = vm::loadHot,
        onOpenTag = onOpenTag
    )
}

@Composable
private fun TagHubScreen(
    state: TagHubUiState,
    onKeywordChange: (String) -> Unit,
    onSearch: () -> Unit,
    onRetry: () -> Unit,
    onOpenTag: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.keyword,
                onValueChange = onKeywordChange,
                modifier = Modifier.weight(1f),
                label = { Text(stringResource(R.string.community_publish_search_tag_label)) }
            )
            Button(onClick = onSearch, modifier = Modifier.padding(top = 8.dp)) {
                Text(stringResource(R.string.community_publish_search))
            }
        }
        if (state.loading || state.searching) {
            CircularProgressIndicator()
        }
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = onRetry)
        }
        Text(stringResource(R.string.tag_hub_hot), style = MaterialTheme.typography.titleMedium)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.hotTags, key = { it.tagId }) { tag ->
                TagCard(tag = tag, onOpenTag = onOpenTag)
            }
            if (state.searchTags.isNotEmpty()) {
                item {
                    Text(stringResource(R.string.tag_hub_search_result), style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 8.dp))
                }
            }
            items(state.searchTags, key = { "search-${it.tagId}" }) { tag ->
                TagCard(tag = tag, onOpenTag = onOpenTag)
            }
        }
    }
}

@Composable
private fun TagCard(tag: CommunityTag, onOpenTag: (Long) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenTag(tag.tagId) }
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text("#${tag.name}", style = MaterialTheme.typography.titleMedium)
            Text(stringResource(R.string.tag_meta_format, tag.contentCount, tag.hotScore))
        }
    }
}



