package com.animegen.app.ai

internal object LlamaCppNativeBridge {
    @Volatile
    private var loadTried = false

    @Volatile
    private var loaded = false

    fun isAvailable(): Boolean {
        ensureLoaded()
        return loaded
    }

    private fun ensureLoaded() {
        if (loadTried) return
        synchronized(this) {
            if (loadTried) return
            loaded = runCatching {
                System.loadLibrary("animegen_llama")
                true
            }.getOrDefault(false)
            loadTried = true
        }
    }

    external fun infer(
        modelPath: String,
        prompt: String,
        maxTokens: Int,
        temperature: Float,
        topP: Float,
        seed: Int,
        threads: Int,
        contextSize: Int
    ): String?
}
