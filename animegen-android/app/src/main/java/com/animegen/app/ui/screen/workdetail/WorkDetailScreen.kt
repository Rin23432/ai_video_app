package com.animegen.app.ui.screen.workdetail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.animegen.app.AppContainer
import com.animegen.app.R
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory

@Composable
fun WorkDetailRoute(container: AppContainer, workIdArg: String?, onPublish: (Long) -> Unit) {
    val vm: WorkDetailViewModel = viewModel(factory = viewModelFactory {
        WorkDetailViewModel(container.worksRepository)
    })
    val state by vm.uiState.collectAsState()
    val workId = workIdArg?.toLongOrNull()

    LaunchedEffect(workId) {
        if (workId != null) vm.load(workId)
    }

    WorkDetailScreen(
        state = state,
        onRetry = { if (workId != null) vm.load(workId) },
        onPublish = { if (workId != null) onPublish(workId) }
    )
}

@Composable
private fun WorkDetailScreen(state: WorkDetailUiState, onRetry: () -> Unit, onPublish: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(stringResource(R.string.work_detail_title))
        if (state.loading) CircularProgressIndicator()
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = onRetry)
            return
        }

        val work = state.work
        if (work == null) {
            Text(stringResource(R.string.work_detail_empty))
            Button(onClick = onRetry) { Text(stringResource(R.string.common_retry)) }
            return
        }

        val statusText = when (work.status.uppercase()) {
            "PENDING" -> stringResource(R.string.status_pending)
            "RUNNING" -> stringResource(R.string.status_running)
            "READY" -> stringResource(R.string.status_ready)
            "SUCCESS" -> stringResource(R.string.status_success)
            "FAIL", "FAILED" -> stringResource(R.string.status_fail)
            else -> work.status
        }
        Text(work.title)
        Text(stringResource(R.string.work_detail_status_format, statusText))
        if (work.status == "READY") {
            Button(onClick = onPublish) { Text(stringResource(R.string.work_detail_publish)) }
        }
        if (!work.videoUrl.isNullOrBlank()) {
            VideoPlayer(url = work.videoUrl)
        } else {
            Text(stringResource(R.string.work_detail_no_video))
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
        factory = {
            PlayerView(it).apply {
                this.player = player
                useController = true
            }
        },
        modifier = Modifier.fillMaxSize()
    )
}





