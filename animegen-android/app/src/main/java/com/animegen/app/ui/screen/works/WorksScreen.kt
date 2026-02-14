package com.animegen.app.ui.screen.works

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
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.animegen.app.AppContainer
import com.animegen.app.data.network.Work
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory

@Composable
fun WorksRoute(container: AppContainer, onOpenDetail: (Long) -> Unit) {
    val vm: WorksViewModel = viewModel(factory = viewModelFactory {
        WorksViewModel(container.worksRepository)
    })
    val state by vm.uiState.collectAsState()

    LaunchedEffect(Unit) { vm.load() }

    Column(modifier = Modifier.fillMaxSize()) {
        if (state.loading) CircularProgressIndicator(modifier = Modifier.padding(16.dp))
        if (state.errorMessage != null) {
            ErrorNotice(message = state.errorMessage, onRetry = vm::load)
        }
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(state.works, key = { it.id }) { work ->
                WorkCard(work = work, onClick = { onOpenDetail(work.id) })
            }
        }
    }
}

@Composable
private fun WorkCard(work: Work, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(modifier = Modifier.padding(12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            AsyncImage(
                model = work.coverUrl,
                contentDescription = work.title,
                modifier = Modifier.height(84.dp).fillMaxWidth(0.3f),
                contentScale = ContentScale.Crop
            )
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(work.title)
                Text("状态: ${work.status}")
            }
        }
    }
}

