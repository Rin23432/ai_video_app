package com.animegen.app.ui.screen.community

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
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
import com.animegen.app.ai.TagSuggestEngine
import com.animegen.app.data.network.CommunityTag
import com.animegen.app.data.repo.AppResult
import com.animegen.app.data.repo.CommunityRepository
import com.animegen.app.observability.Telemetry
import com.animegen.app.ui.common.ErrorNotice
import com.animegen.app.ui.common.viewModelFactory
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class CommunityPublishUiState(
    val title: String = "",
    val description: String = "",
    val keyword: String = "",
    val posting: Boolean = false,
    val searchingTag: Boolean = false,
    val suggestingLocalTag: Boolean = false,
    val hotTags: List<CommunityTag> = emptyList(),
    val searchTags: List<CommunityTag> = emptyList(),
    val selectedTagIds: Set<Long> = emptySet(),
    val errorMessage: String? = null
)

class CommunityPublishViewModel(
    private val repository: CommunityRepository,
    private val tagSuggestEngine: TagSuggestEngine
) : ViewModel() {
    private val _uiState = MutableStateFlow(CommunityPublishUiState())
    val uiState: StateFlow<CommunityPublishUiState> = _uiState.asStateFlow()

    fun loadHotTags() {
        viewModelScope.launch {
            when (val result = repository.hotTags()) {
                is AppResult.Success -> _uiState.update {
                    it.copy(hotTags = result.data.items, errorMessage = null)
                }
                is AppResult.Failure -> _uiState.update { it.copy(errorMessage = result.error.displayMessage) }
            }
        }
    }

    fun setTitle(value: String) = _uiState.update { it.copy(title = value) }
    fun setDescription(value: String) = _uiState.update { it.copy(description = value) }
    fun setKeyword(value: String) = _uiState.update { it.copy(keyword = value) }

    fun suggestTagsLocal() {
        val title = _uiState.value.title.trim()
        val description = _uiState.value.description.trim().ifBlank { null }
        if (title.isBlank() && description.isNullOrBlank()) {
            _uiState.update { it.copy(errorMessage = "请先输入标题或描述") }
            return
        }

        val candidates = (_uiState.value.hotTags + _uiState.value.searchTags)
            .distinctBy { it.tagId }
        if (candidates.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "暂无可推荐标签，请先加载热门标签") }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(suggestingLocalTag = true, errorMessage = null) }
            val suggestions = tagSuggestEngine.suggest(
                title = title,
                description = description,
                candidates = candidates,
                topK = 5
            )

            val selected = _uiState.value.selectedTagIds.toMutableSet()
            suggestions.forEach { if (selected.size < 5) selected.add(it.tagId) }
            val actualEngine = suggestions.firstOrNull()?.reason ?: tagSuggestEngine.engineName

            Telemetry.event(
                name = "local_tag_suggest",
                attrs = mapOf(
                    "candidate_count" to candidates.size.toString(),
                    "suggest_count" to suggestions.size.toString(),
                    "selected_count" to selected.size.toString(),
                    "engine" to actualEngine
                )
            )

            _uiState.update {
                it.copy(
                    suggestingLocalTag = false,
                    selectedTagIds = selected,
                    errorMessage = if (suggestions.isEmpty()) "未命中推荐标签，可手动搜索后重试" else null
                )
            }
        }
    }

    fun searchTag() {
        val keyword = _uiState.value.keyword.trim()
        if (keyword.isBlank()) {
            _uiState.update { it.copy(searchTags = emptyList()) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(searchingTag = true, errorMessage = null) }
            when (val result = repository.searchTags(keyword)) {
                is AppResult.Success -> _uiState.update {
                    it.copy(searchingTag = false, searchTags = result.data.items)
                }
                is AppResult.Failure -> _uiState.update {
                    it.copy(searchingTag = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }

    fun toggleTag(tagId: Long) {
        _uiState.update { old ->
            val mutable = old.selectedTagIds.toMutableSet()
            if (!mutable.add(tagId)) {
                mutable.remove(tagId)
            } else if (mutable.size > 5) {
                mutable.remove(tagId)
                return@update old.copy(errorMessage = "最多只能选择 5 个标签")
            }
            old.copy(selectedTagIds = mutable, errorMessage = null)
        }
    }

    fun publish(workId: Long, onDone: (Long) -> Unit) {
        val title = _uiState.value.title.trim()
        if (title.isBlank()) {
            _uiState.update { it.copy(errorMessage = "标题不能为空") }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(posting = true, errorMessage = null) }
            when (
                val result = repository.publish(
                    workId = workId,
                    title = title,
                    description = _uiState.value.description.trim().ifBlank { null },
                    tagIds = _uiState.value.selectedTagIds.toList()
                )
            ) {
                is AppResult.Success -> {
                    _uiState.update { it.copy(posting = false) }
                    onDone(result.data.contentId)
                }

                is AppResult.Failure -> _uiState.update {
                    it.copy(posting = false, errorMessage = result.error.displayMessage)
                }
            }
        }
    }
}

@Composable
fun CommunityPublishRoute(container: AppContainer, workIdArg: String?, onDone: (Long) -> Unit) {
    val vm: CommunityPublishViewModel = viewModel(factory = viewModelFactory {
        CommunityPublishViewModel(
            repository = container.communityRepository,
            tagSuggestEngine = container.tagSuggestEngine
        )
    })
    val state by vm.uiState.collectAsState()
    val workId = workIdArg?.toLongOrNull()

    LaunchedEffect(Unit) {
        vm.loadHotTags()
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Text(stringResource(R.string.community_publish_title))
        Text(stringResource(R.string.community_publish_work_id_format, (workId?.toString() ?: stringResource(R.string.common_invalid))))
        OutlinedTextField(
            value = state.title,
            onValueChange = vm::setTitle,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.community_publish_title_label)) }
        )
        OutlinedTextField(
            value = state.description,
            onValueChange = vm::setDescription,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.community_publish_desc_label)) }
        )
        OutlinedTextField(
            value = state.keyword,
            onValueChange = vm::setKeyword,
            modifier = Modifier.fillMaxWidth(),
            label = { Text(stringResource(R.string.community_publish_search_tag_label)) }
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = vm::searchTag) {
                Text(stringResource(R.string.community_publish_search))
            }
            Button(onClick = vm::suggestTagsLocal, enabled = !state.suggestingLocalTag) {
                if (state.suggestingLocalTag) {
                    CircularProgressIndicator()
                } else {
                    Text(stringResource(R.string.community_publish_local_suggest))
                }
            }
        }
        Text(stringResource(R.string.community_publish_selected_tags_format, state.selectedTagIds.size))
        TagPickerRow(
            tags = state.hotTags,
            selectedTagIds = state.selectedTagIds,
            onToggle = vm::toggleTag
        )
        if (state.searchingTag) {
            CircularProgressIndicator()
        }
        if (state.searchTags.isNotEmpty()) {
            Text(stringResource(R.string.community_publish_search_result))
            TagPickerRow(
                tags = state.searchTags,
                selectedTagIds = state.selectedTagIds,
                onToggle = vm::toggleTag
            )
        }
        val errorMessage = state.errorMessage
        if (errorMessage != null) {
            ErrorNotice(message = errorMessage, onRetry = vm::loadHotTags)
        }
        Button(
            onClick = { if (workId != null) vm.publish(workId, onDone) },
            enabled = !state.posting && workId != null
        ) {
            if (state.posting) CircularProgressIndicator()
            else Text(stringResource(R.string.community_publish_submit))
        }
    }
}

@Composable
private fun TagPickerRow(
    tags: List<CommunityTag>,
    selectedTagIds: Set<Long>,
    onToggle: (Long) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        tags.forEach { tag ->
            FilterChip(
                selected = selectedTagIds.contains(tag.tagId),
                onClick = { onToggle(tag.tagId) },
                label = { Text("#${tag.name}") }
            )
        }
    }
}

