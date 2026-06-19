package com.allset.allset.util

import java.net.URI
import java.net.URLDecoder

object LocationUtils {

    /**
     * Best-effort extraction of a human-readable place name from a Google Maps
     * (or similar) URL.  Returns null when the URL format is unrecognised.
     *
     * Supported formats:
     *   google.com/maps/place/Place+Name/...
     *   google.com/maps/place/Place+Name/@lat,lng,...
     *   google.com/maps?q=Place+Name
     *   google.com/maps/search/Place+Name/...
     *   maps.google.com/...  (same path patterns)
     */
    fun extractPlaceName(url: String?): String? {
        if (url.isNullOrBlank()) return null

        return try {
            val uri = URI(url.trim())
            val host = uri.host?.lowercase() ?: return null
            val path = uri.path ?: ""
            val query = uri.query

            when {
                host.contains("google") || host.contains("goo.gl") -> extractFromGoogle(path, query)
                host.contains("yandex") -> extractFromYandex(path, query)
                else -> null
            }
        } catch (_: Exception) {
            null
        }
    }

    private fun extractFromGoogle(path: String, query: String?): String? {
        // /maps/place/Place+Name/... or /maps/place/Place+Name/@...
        val placeRegex = Regex("/maps/place/([^/@]+)")
        placeRegex.find(path)?.let { match ->
            return decode(match.groupValues[1])
        }

        // /maps/search/Place+Name/...
        val searchRegex = Regex("/maps/search/([^/@]+)")
        searchRegex.find(path)?.let { match ->
            return decode(match.groupValues[1])
        }

        // ?q=Place+Name
        if (query != null) {
            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                parts[0] to (parts.getOrNull(1) ?: "")
            }
            params["q"]?.takeIf { it.isNotBlank() && !isCoordinates(it) }?.let {
                return decode(it)
            }
        }

        return null
    }

    private fun extractFromYandex(path: String, query: String?): String? {
        if (query != null) {
            val params = query.split("&").associate {
                val parts = it.split("=", limit = 2)
                parts[0] to (parts.getOrNull(1) ?: "")
            }
            params["text"]?.takeIf { it.isNotBlank() }?.let {
                return decode(it)
            }
        }
        return null
    }

    private fun decode(value: String): String {
        return URLDecoder.decode(value, "UTF-8").replace("+", " ").trim()
    }

    private fun isCoordinates(value: String): Boolean {
        return value.matches(Regex("^-?\\d+\\.\\d+,\\s*-?\\d+\\.\\d+$"))
    }
}
