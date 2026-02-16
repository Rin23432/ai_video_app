package com.animegen.app.ai

import com.animegen.app.data.network.CommunityTag

class FallbackTagSuggestEngine(
    private val primary: TagSuggestEngine,
    private val fallback: TagSuggestEngine
) : TagSuggestEngine {
    override val engineName: String
        get() = "${primary.engineName}_with_${fallback.engineName}"

    override suspend fun suggest(
        title: String,
        description: String?,
        candidates: List<CommunityTag>,
        topK: Int
    ): List<TagScore> {
        return runCatching {
            primary.suggest(title, description, candidates, topK)
        }.getOrElse { emptyList() }
            .ifEmpty {
                fallback.suggest(title, description, candidates, topK)
            }
    }
}
