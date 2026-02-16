package com.animegen.app.observability

import android.util.Log

object Telemetry {
    private const val TAG = "AnimeGenTelemetry"

    fun event(name: String, attrs: Map<String, String>) {
        val body = attrs.entries.joinToString(separator = ", ") { "${it.key}=${it.value}" }
        Log.i(TAG, "$name | $body")
    }
}
