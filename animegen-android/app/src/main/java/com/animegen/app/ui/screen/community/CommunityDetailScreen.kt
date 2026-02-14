package com.animegen.app.ui.screen.community

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.animegen.app.AppContainer
import com.animegen.app.data.network.CommunityComment
import com.animegen.app.data.network.CommunityContentDetail
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityDetailUiState(
    val loading: Boolean = false,
    val content: CommunityContentDetail? = null,
    val comments: List<CommunityComment> = emptyList(),
    val commentInput: String = "",
    val submittingComment: Boolean = false,
    val errorMessage: String? = null
)

class CommunityDetailViewModel(
    private val repository: CommunityRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityDetailUiState())
    val uiState: StateFlow<CommunityDetailUiState> = _uiState.asStateFlow()

    fun load(contentId: Long) {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, errorMessage = null) }
            val detail = repository.detail(contentId)
            val comments = repository.comments(contentId)
            if (detail is AppResult.Success && comments is AppResult.Success) {
                _uiState.update {
                    it.copy(
                        loading = false,
                        content = detail.data,
                        comments = comments.data.items
                    )
                }
            } else {
                val message = when {
                    detail is AppResult.Failure -> detail.error.displayMessage
                    comments is AppResult.Failure -> comments.error.displayMessage
                    else -> "load failed"
                }
                _uiState.update { it.copy(loading = false, errorMessage = message) }
            }
        }
    }

    fun setCommentInput(value: String) {
        _uiState.update { it.copy(commentInput = value) }
    }

    fun toggleLike(contentId: Long) {
        val snapshot = _uiState.value.content ?: return
        val optimisticLiked = !snapshot.viewerState.liked
        val optimisticCount = (snapshot.likeCount + if (optimisticLiked) 1 else -1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                content = snapshot.copy(
                    likeCount = optimisticCount,
                    viewerState = snapshot.viewerState.copy(liked = optimisticLiked)
                )
            )
        }
        viewModelScope.launch {
            when (val result = repository.toggleLike(contentId)) {
                is AppResult.Success -> {
                    val latest = _uiState.value.content ?: return@launch
                    val serverLiked = result.data.liked ?: latest.viewerState.liked
                    val serverCount = result.data.likeCount ?: latest.likeCount
                    _uiState.update {
                        it.copy(content = latest.copy(
                            likeCount = serverCount,
                            viewerState = latest.viewerState.copy(liked = serverLiked)
                        ))
                    }
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(content = snapshot, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun toggleFavorite(contentId: Long) {
        val snapshot = _uiState.value.content ?: return
        val optimisticFav = !snapshot.viewerState.favorited
        val optimisticCount = (snapshot.favoriteCount + if (optimisticFav) 1 else -1).coerceAtLeast(0)
        _uiState.update {
            it.copy(
                content = snapshot.copy(
                    favoriteCount = optimisticCount,
                    viewerState = snapshot.viewerState.copy(favorited = optimisticFav)
                )
            )
        }
        viewModelScope.launch {
            when (val result = repository.toggleFavorite(contentId)) {
                is AppResult.Success -> {
                    val latest = _uiState.value.content ?: return@launch
                    val serverFav = result.data.favorited ?: latest.viewerState.favorited
                    val serverCount = result.data.favoriteCount ?: latest.favoriteCount
                    _uiState.update {
                        it.copy(content = latest.copy(
                            favoriteCount = serverCount,
                            viewerState = latest.viewerState.copy(favorited = serverFav)
                        ))
                    }
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(content = snapshot, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun sendComment(contentId: Long) {
        val text = _uiState.value.commentInput.trim()
        if (text.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(submittingComment = true) }
            when (val result = repository.createComment(contentId, text)) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(submittingComment = false, commentInput = "") }
                    val list = repository.comments(contentId)
                    if (list is AppResult.Success) {
                        val detail = _uiState.value.content
                        _uiState.update {
                            it.copy(
                                comments = list.data.items,
                                content = detail?.copy(commentCount = result.data.commentCount)
                            )
                        }
                    }
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(submittingComment = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun CommunityDetailRoute(container: AppContainer, contentIdArg: String?) {
    val vm: CommunityDetailViewModel = viewModel(factory = viewModelFactory {
        CommunityDetailViewModel(container.communityRepository)
    })
    val state by vm.uiState.collectAsState()
    val contentId = contentIdArg?.toLongOrNull()

    LaunchedEffect(contentId) { if (contentId != null) vm.load(contentId) }

    CommunityDetailScreen(
        state = state,
        onRetry = { if (contentId != null) vm.load(contentId) },
        onLike = { if (contentId != null) vm.toggleLike(contentId) },
        onFavorite = { if (contentId != null) vm.toggleFavorite(contentId) },
        onCommentChange = vm::setCommentInput,
        onSendComment = { if (contentId != null) vm.sendComment(contentId) }
    )
}

@Composable
private fun CommunityDetailScreen(
    state: CommunityDetailUiState,
    onRetry: () -> Unit,
    onLike: () -> Unit,
    onFavorite: () -> Unit,
    onCommentChange: (String) -> Unit,
    onSendComment: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize().padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        if (state.loading) CircularProgressIndicator()
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = onRetry)
        }
        val content = state.content ?: return
        Text(content.title, style = MaterialTheme.typography.titleLarge)
        Text("by ${content.author.nickname}")
        if (!content.mediaUrl.isNullOrBlank()) {
            VideoPlayer(url = content.mediaUrl)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = onLike) { Text(if (content.viewerState.liked) "Liked ${content.likeCount}" else "Like ${content.likeCount}") }
            Button(onClick = onFavorite) { Text(if (content.viewerState.favorited) "Favorited ${content.favoriteCount}" else "Favorite ${content.favoriteCount}") }
            Text("Comments ${content.commentCount}", modifier = Modifier.padding(top = 10.dp))
        }
        OutlinedTextField(
            value = state.commentInput,
            onValueChange = onCommentChange,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Write a comment") }
        )
        Button(onClick = onSendComment, enabled = !state.submittingComment) {
            Text(if (state.submittingComment) "Sending..." else "Send")
        }
        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(state.comments, key = { it.commentId }) { comment ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(comment.user.nickname, style = MaterialTheme.typography.labelLarge)
                        Text(comment.text)
                    }
                }
            }
        }
    }
}

@Composable
private fun VideoPlayer(url: String) {
    val context = LocalContext.current
    val player = remember(url) {
        ExoPlayer.Builder(context).build().apply {
            setMediaItem(MediaItem.fromUri(url))
            prepare()
            playWhenReady = true
        }
    }
    DisposableEffect(player) {
        onDispose { player.release() }
    }
    AndroidView(
        factory = { PlayerView(it).apply { this.player = player } },
        modifier = Modifier.fillMaxWidth().height(220.dp)
    )
}
