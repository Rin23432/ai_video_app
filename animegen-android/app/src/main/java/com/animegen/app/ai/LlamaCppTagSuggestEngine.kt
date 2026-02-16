package com.animegen.app.ai

import android.content.Context
import com.animegen.app.data.network.CommunityTag
import com.google.gson.JsonObject
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import kotlin.math.max

class LlamaCppTagSuggestEngine(
    context: Context,
    private val modelRelativePath: String = "models/qwen2.5-3b-instruct-q4_k_m.gguf",
    private val maxTokens: Int = 96,
    private val temperature: Float = 0.2f,
    private val topP: Float = 0.9f,
    private val contextSize: Int = 1024
) : TagSuggestEngine {
    override val engineName: String = "llama_cpp"

    private val modelFile: File = File(context.filesDir, modelRelativePath)
    private val workerThreads: Int = max(1, Runtime.getRuntime().availableProcessors() / 2)

    override suspend fun suggest(
        title: String,
        description: String?,
        candidates: List<CommunityTag>,
        topK: Int
    ): List<TagScore> = withContext(Dispatchers.Default) {
        if (topK <= 0 || candidates.isEmpty()) return@withContext emptyList()
        if (!modelFile.exists() || modelFile.length() <= 0L) return@withContext emptyList()
        if (!LlamaCppNativeBridge.isAvailable()) return@withContext emptyList()

        val prompt = buildPrompt(
            title = title,
            description = description.orEmpty(),
            candidates = candidates,
            topK = topK
        )

        val raw = runCatching {
            LlamaCppNativeBridge.infer(
                modelPath = modelFile.absolutePath,
                prompt = prompt,
                maxTokens = maxTokens,
                temperature = temperature,
                topP = topP,
                seed = 42,
                threads = workerThreads,
                contextSize = contextSize
            )
        }.getOrNull() ?: return@withContext emptyList()

        val names = parseTagNames(raw)
        if (names.isEmpty()) return@withContext emptyList()

        val byName = candidates.associateBy { normalize(it.name) }
        val ranked = names.mapIndexedNotNull { idx, name ->
            val hit = byName[normalize(name)] ?: return@mapIndexedNotNull null
            val score = 1.0 / (idx + 1)
            TagScore(hit.tagId, hit.name, score, "llama_cpp")
        }

        ranked.distinctBy { it.tagId }.take(topK)
    }

    private fun buildPrompt(
        title: String,
        description: String,
        candidates: List<CommunityTag>,
        topK: Int
    ): String {
        val candidateNames = candidates.joinToString(separator = ", ") { it.name }
        return """
你是内容标签推荐助手。请根据标题和描述，从候选标签中选出最相关的标签。
要求：
1) 只能从候选标签里选；
2) 返回不超过 $topK 个；
3) 仅输出 JSON，格式：{"tags":["标签1","标签2"]}。

标题：$title
描述：$description
候选标签：$candidateNames
""".trimIndent()
    }

    private fun parseTagNames(raw: String): List<String> {
        val jsonBlock = extractJsonObject(raw) ?: return emptyList()
        val root = runCatching { JsonParser.parseString(jsonBlock).asJsonObject }.getOrNull() ?: return emptyList()
        val tagsElement = root.get("tags") ?: return emptyList()
        if (!tagsElement.isJsonArray) return emptyList()
        val tags = tagsElement.asJsonArray
        return tags
            .asSequence()
            .mapNotNull { item ->
                when {
                    item.isJsonPrimitive && item.asJsonPrimitive.isString -> item.asString
                    item.isJsonObject -> item.asJsonObject.extractTagName()
                    else -> null
                }
            }
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .toList()
    }

    private fun JsonObject.extractTagName(): String? {
        return when {
            has("name") && get("name").isJsonPrimitive -> get("name").asString
            has("tag") && get("tag").isJsonPrimitive -> get("tag").asString
            else -> null
        }
    }

    private fun extractJsonObject(raw: String): String? {
        val start = raw.indexOf('{')
        val end = raw.lastIndexOf('}')
        if (start < 0 || end <= start) return null
        return raw.substring(start, end + 1)
    }

    private fun normalize(value: String): String = value.trim().lowercase()
}
