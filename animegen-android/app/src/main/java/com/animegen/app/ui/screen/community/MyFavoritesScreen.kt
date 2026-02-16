package com.animegen.app.ui.screen.community

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
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

data class MyFavoritesUiState(
    val loading: Boolean = false,
    val items: List<CommunityContentSummary> = emptyList(),
    val errorMessage: String? = null
)

class MyFavoritesViewModel(private val repository: CommunityRepository) : ViewModel() {
    private val _uiState = MutableStateFlow(MyFavoritesUiState())
    val uiState: StateFlow<MyFavoritesUiState> = _uiState.asStateFlow()

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            when (val result = repository.myFavorites()) {
                is AppResult.Success -> _uiState.update { it.copy(loading = false, items = result.data.items) }
                is AppResult.Failure -> _uiState.update { it.copy(loading = false, errorMessage = result.error.displayMessage) }
            }
        }
    }
}

@Composable
fun MyFavoritesRoute(container: AppContainer, onOpenDetail: (Long) -> Unit) {
    val vm: MyFavoritesViewModel = viewModel(factory = viewModelFactory { MyFavoritesViewModel(container.communityRepository) })
    val state by vm.uiState.collectAsState()
    LaunchedEffect(Unit) { vm.load() }

    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(stringResource(R.string.my_favorites_title))
        if (state.loading) CircularProgressIndicator()
        val errorMessage = state.errorMessage
        if (errorMessage != null) ErrorNotice(message = errorMessage, onRetry = vm::load)
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.items, key = { it.contentId }) {
                Text("${it.title} (${it.favoriteCount})", modifier = Modifier.fillMaxWidth().clickable { onOpenDetail(it.contentId) }.padding(6.dp))
            }
        }
    }
}




