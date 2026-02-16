package com.animegen.app.ai

import com.animegen.app.data.network.CommunityTag
import kotlin.math.max

class LocalTagSuggestEngine : TagSuggestEngine {
    override val engineName: String = "local_rule"

    private val keywordHints: Map<String, List<String>> = mapOf(
        "恋爱" to listOf("恋爱", "心动", "告白", "情侣"),
        "校园" to listOf("校园", "学生", "社团", "课堂"),
        "奇幻" to listOf("奇幻", "魔法", "异世界", "精灵"),
        "冒险" to listOf("冒险", "旅途", "探险", "闯关"),
        "搞笑" to listOf("搞笑", "沙雕", "喜剧", "整活"),
        "悬疑" to listOf("悬疑", "推理", "谜团", "反转"),
        "科幻" to listOf("科幻", "机甲", "未来", "太空"),
        "同人" to listOf("同人", "二创", "衍生")
    )

    override suspend fun suggest(
        title: String,
        description: String?,
        candidates: List<CommunityTag>,
        topK: Int
    ): List<TagScore> {
        val text = normalize("$title ${description.orEmpty()}")
        if (text.isBlank() || candidates.isEmpty() || topK <= 0) return emptyList()

        val scored = candidates.mapNotNull { tag ->
            val tagName = normalize(tag.name)
            if (tagName.isBlank()) return@mapNotNull null

            var score = 0.0
            val reason = mutableListOf<String>()

            if (text.contains(tagName)) {
                score += 10.0
                reason += "exact"
            }

            val overlap = overlapRatio(text, tagName)
            if (overlap > 0.0) {
                score += overlap * 6.0
                reason += "overlap"
            }

            val hintMatches = keywordHints
                .filterKeys { tagName.contains(it) }
                .values
                .flatten()
                .count { hint -> text.contains(normalize(hint)) }
            if (hintMatches > 0) {
                score += hintMatches * 2.0
                reason += "hint"
            }

            score += (tag.hotScore.coerceAtLeast(0L) / 10000.0)

            if (score <= 0.0) return@mapNotNull null
            TagScore(tag.tagId, tag.name, score, reason.joinToString("+"))
        }

        return scored
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun normalize(value: String): String = value.lowercase().trim()

    private fun overlapRatio(text: String, tag: String): Double {
        if (text.isBlank() || tag.isBlank()) return 0.0
        val textSet = text.toSet()
        val tagSet = tag.toSet()
        val inter = textSet.intersect(tagSet).size.toDouble()
        return inter / max(tagSet.size, 1)
    }
}
