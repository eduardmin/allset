package com.allset.allset.util

import com.ibm.icu.text.Transliterator

/**
 * Builds URL-safe slug segments from localized titles. Non-Latin scripts are
 * transliterated (ICU Any-Latin → Latin-ASCII) before ASCII slug rules apply.
 */
object InvitationSlug {

    private val toAsciiLatin: Transliterator by lazy {
        Transliterator.getInstance("Any-Latin; Latin-ASCII")
    }

    private val preferredLocaleOrder = listOf("en", "hy", "ru")

    fun pickTitleText(title: Map<String, String>): String {
        for (locale in preferredLocaleOrder) {
            title[locale]?.takeIf { it.isNotBlank() }?.let { return it.trim() }
        }
        return title.values.firstOrNull { it.isNotBlank() }?.trim().orEmpty()
    }

    fun baseSlugSegment(title: Map<String, String>): String {
        val raw = pickTitleText(title).ifBlank { "invitation" }
        val transliterated = toAsciiLatin.transliterate(raw)
        return transliterated
            .lowercase()
            .replace(Regex("[^a-z0-9\\s-]"), "")
            .trim()
            .replace(Regex("\\s+"), "-")
            .replace(Regex("-+"), "-")
            .take(60)
            .trimEnd('-')
            .ifEmpty { "invitation" }
    }
}
