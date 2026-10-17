package com.clipvault.app

import android.net.Uri

object UrlCleaner {
    private val TRACKING_PARAMS = setOf(
        "utm_source", "utm_medium", "utm_campaign", "utm_term", "utm_content",
        "utm_id", "fbclid", "gclid", "dclid", "msclkid", "twclid", "yclid",
        "igshid", "si", "feature", "mc_cid", "mc_eid", "_ga", "_gl", "ref",
        "ref_src", "ref_url", "spm", "scm", "aff_platform", "aff_trace_key"
    )

    fun clean(rawUrl: String): Pair<String, Boolean> {
        val trimmed = rawUrl.trim()
        if (!trimmed.startsWith("http://") && !trimmed.startsWith("https://")) {
            return Pair(rawUrl, false)
        }

        try {
            val uri = Uri.parse(trimmed)
            val queryNames = uri.queryParameterNames ?: return Pair(rawUrl, false)
            
            val hasTracking = queryNames.any { name ->
                name.lowercase() in TRACKING_PARAMS || name.lowercase().startsWith("utm_")
            }

            if (!hasTracking) return Pair(rawUrl, false)

            val newUriBuilder = uri.buildUpon().clearQuery()
            for (name in queryNames) {
                if (name.lowercase() !in TRACKING_PARAMS && !name.lowercase().startsWith("utm_")) {
                    for (value in uri.getQueryParameters(name)) {
                        newUriBuilder.appendQueryParameter(name, value)
                    }
                }
            }

            return Pair(newUriBuilder.build().toString(), true)
        } catch (e: Exception) {
            return Pair(rawUrl, false)
        }
    }
}
