package com.animegen.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animegen.app.AppContainer
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
    val errorMessage: String? = null
)

class CommunityFeedViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityFeedUiState())
    val uiState: StateFlow<CommunityFeedUiState> = _uiState.asStateFlow()

    fun load(tab: String = _uiState.value.tab) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null, tab = tab) }
            when (val result = repository.feed(tab = tab)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(loading = false, items = result.data.items)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(loading = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun CommunityFeedRoute(container: AppContainer, onOpenDetail: (Long) -> Unit, onOpenFavorites: () -> Unit, onOpenPublished: () -> Unit) {
    val vm: CommunityFeedViewModel = viewModel(factory = viewModelFactory {
        CommunityFeedViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = { vm.load("latest") }, enabled = state.tab != "latest") { Text("Latest") }
            Button(onClick = { vm.load("hot") }, enabled = state.tab != "hot") { Text("Hot") }
            OutlinedButton(onClick = onOpenFavorites) { Text("MyFavorites") }
            OutlinedButton(onClick = onOpenPublished) { Text("MyPublished") }
        }
        if (state.loading) CircularProgressIndicator()
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = { vm.load(state.tab) })
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.items, key = { it.contentId }) { item ->
                Card(modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(item.contentId) }) {
                    Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        AsyncImage(
                            model = item.coverUrl,
                            contentDescription = item.title,
                            modifier = Modifier.width(120.dp).height(80.dp),
                            contentScale = ContentScale.Crop
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            Text(item.title, style = MaterialTheme.typography.titleMedium)
                            Text("by ${item.author.nickname}")
                            Text("Like ${item.likeCount} | Fav ${item.favoriteCount} | Cmt ${item.commentCount}")
                        }
                    }
                }
            }
        }
    }
}
