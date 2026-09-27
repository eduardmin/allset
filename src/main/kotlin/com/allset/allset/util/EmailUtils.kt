package com.allset.allset.util

/**
 * Canonical form of an email used for both storage and lookup, so that
 * "Case@X.com" and "case@x.com" resolve to a single user. Trims surrounding
 * whitespace and lowercases. Callers must normalize before persisting or
 * querying by email to keep the unique index effective.
 */
object EmailUtils {
    fun normalize(email: String): String = email.trim().lowercase()
}
