package com.allset.allset.util

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class InvitationSlugTest {

    @Test
    fun `Armenian-only title produces Latin slug`() {
        val slug = InvitationSlug.baseSlugSegment(
            mapOf("hy" to "Էդուարդ, Լիա")
        )
        assertEquals("eduard-lia", slug)
    }

    @Test
    fun `prefers English when present`() {
        val slug = InvitationSlug.baseSlugSegment(
            mapOf("hy" to "Հարսանիք", "en" to "Our Wedding")
        )
        assertEquals("our-wedding", slug)
    }

    @Test
    fun `falls back to invitation when title empty`() {
        assertEquals("invitation", InvitationSlug.baseSlugSegment(emptyMap()))
        assertEquals(
            "invitation",
            InvitationSlug.baseSlugSegment(mapOf("en" to "   ", "hy" to ""))
        )
    }
}
