package com.animegen.app.ai

import com.animegen.app.data.network.CommunityTag

data class TagScore(
    val tagId: Long,
    val name: String,
    val score: Double,
    val reason: String
)

interface TagSuggestEngine {
    val engineName: String

    suspend fun suggest(
        title: String,
        description: String?,
        candidates: List<CommunityTag>,
        topK: Int = 3
    ): List<TagScore>
}
