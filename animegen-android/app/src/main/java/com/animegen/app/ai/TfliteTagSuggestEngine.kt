package com.animegen.app.ai

import android.content.Context
import com.animegen.app.data.network.CommunityTag
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import java.io.BufferedReader
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import kotlin.math.exp
import kotlin.math.max

class TfliteTagSuggestEngine(
    private val context: Context,
    private val modelAssetName: String = "tag_suggest.tflite",
    private val labelsAssetName: String = "tag_suggest.labels.txt"
) : TagSuggestEngine {
    override val engineName: String = "tflite"

    @Volatile
    private var interpreter: Interpreter? = null
    @Volatile
    private var labelsCache: List<String>? = null

    private val lock = Any()
    private val labelsLock = Any()

    override suspend fun suggest(
        title: String,
        description: String?,
        candidates: List<CommunityTag>,
        topK: Int
    ): List<TagScore> {
        if (candidates.isEmpty() || topK <= 0) return emptyList()
        val model = ensureInterpreter() ?: return emptyList()

        val inputTensor = model.getInputTensor(0)
        val outputTensor = model.getOutputTensor(0)
        if (inputTensor.dataType() != DataType.FLOAT32 || outputTensor.dataType() != DataType.FLOAT32) {
            return emptyList()
        }

        val inputShape = inputTensor.shape()
        val outputShape = outputTensor.shape()
        val featureSize = when {
            inputShape.isEmpty() -> return emptyList()
            inputShape.size == 1 -> inputShape[0]
            else -> inputShape.last()
        }
        if (featureSize <= 0) return emptyList()

        val outputSize = when {
            outputShape.isEmpty() -> return emptyList()
            outputShape.size == 1 -> outputShape[0]
            else -> outputShape.last()
        }
        if (outputSize <= 0) return emptyList()

        val text = "$title ${description.orEmpty()}".trim()
        if (text.isBlank()) return emptyList()

        val inputVector = textToFeature(text, featureSize)
        val outputVector = FloatArray(outputSize)

        val input = arrayOf(inputVector)
        val output = arrayOf(outputVector)

        runCatching { model.run(input, output) }.getOrElse { return emptyList() }

        val usable = minOf(candidates.size, outputSize)
        if (usable <= 0) return emptyList()

        val labels = loadLabels()
        if (labels.isNotEmpty() && labels.size == outputSize) {
            val tagByNormalizedName = candidates.associateBy { normalize(it.name) }
            val probs = softmax(outputVector)
            return labels
                .mapIndexedNotNull { idx, label ->
                    val target = tagByNormalizedName[normalize(label)] ?: return@mapIndexedNotNull null
                    TagScore(
                        tagId = target.tagId,
                        name = target.name,
                        score = probs[idx].toDouble(),
                        reason = "tflite_labels"
                    )
                }
                .sortedByDescending { it.score }
                .take(topK)
        }

        // Compatibility path: if labels are unavailable/mismatched, keep legacy behavior
        // where model output index aligns with candidate order.
        val probs = softmax(outputVector.copyOfRange(0, usable))
        return probs
            .mapIndexed { idx, score ->
                TagScore(
                    tagId = candidates[idx].tagId,
                    name = candidates[idx].name,
                    score = score.toDouble(),
                    reason = "tflite_index"
                )
            }
            .sortedByDescending { it.score }
            .take(topK)
    }

    private fun ensureInterpreter(): Interpreter? {
        interpreter?.let { return it }
        synchronized(lock) {
            interpreter?.let { return it }
            val mappedModel = runCatching { loadModelFile(modelAssetName) }.getOrNull() ?: return null
            val options = Interpreter.Options().apply {
                setNumThreads(2)
            }
            interpreter = runCatching { Interpreter(mappedModel, options) }.getOrNull()
            return interpreter
        }
    }

    private fun textToFeature(text: String, dim: Int): FloatArray {
        val features = FloatArray(dim)
        if (text.isBlank()) return features

        // Hashing trick: map unicode chars into fixed-size bag-of-features.
        text.forEach { ch ->
            val index = (ch.code and Int.MAX_VALUE) % dim
            features[index] += 1f
        }

        val norm = max(1f, text.length.toFloat())
        for (i in features.indices) {
            features[i] /= norm
        }
        return features
    }

    private fun softmax(values: FloatArray): FloatArray {
        if (values.isEmpty()) return values
        val maxValue = values.maxOrNull() ?: 0f
        val expValues = FloatArray(values.size)
        var sum = 0.0
        for (i in values.indices) {
            val v = exp((values[i] - maxValue).toDouble())
            expValues[i] = v.toFloat()
            sum += v
        }
        if (sum <= 0.0) return values
        for (i in expValues.indices) {
            expValues[i] = (expValues[i] / sum).toFloat()
        }
        return expValues
    }

    private fun loadModelFile(assetName: String): MappedByteBuffer {
        val afd = context.assets.openFd(assetName)
        afd.use { fd ->
            val inputStream = fd.createInputStream()
            inputStream.use { stream ->
                val channel = stream.channel
                return channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.declaredLength)
            }
        }
    }

    private fun loadLabels(): List<String> {
        labelsCache?.let { return it }
        synchronized(labelsLock) {
            labelsCache?.let { return it }
            val labels = runCatching {
                context.assets.open(labelsAssetName).use { stream ->
                    BufferedReader(stream.reader()).useLines { lines ->
                        lines
                            .map { it.trim() }
                            .filter { it.isNotBlank() && !it.startsWith("#") }
                            .toList()
                    }
                }
            }.getOrElse { emptyList() }
            labelsCache = labels
            return labels
        }
    }

    private fun normalize(value: String): String = value.trim().lowercase()
}


