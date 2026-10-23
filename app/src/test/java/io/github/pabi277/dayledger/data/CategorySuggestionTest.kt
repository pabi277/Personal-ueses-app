package io.github.pabi277.dayledger.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * F1 / R2a — suggested categories.
 *
 * Owner decision 2026-10-23: suggestions are confirmed with one tap, never applied
 * silently; existing assignments are never overwritten or re-suggested; ambiguous apps get
 * no suggestion; earning apps are never suggested. Every package name here is fictional.
 */
class CategorySuggestionTest {

    private fun suggest(
        pkg: String = "com.example.app",
        hint: AppCategoryHint,
        isLifeSystemApp: Boolean = false,
        isKnownEarningApp: Boolean = false
    ) = CategorySuggestionRules.suggest(pkg, hint, isLifeSystemApp, isKnownEarningApp)

    // --- the mapping itself ------------------------------------------------------

    @Test
    fun `games video social and audio are suggested as Social over Waste`() {
        listOf(
            AppCategoryHint.GAME,
            AppCategoryHint.VIDEO,
            AppCategoryHint.SOCIAL,
            AppCategoryHint.AUDIO
        ).forEach { hint ->
            assertEquals("$hint should be waste", CategoryNames.WASTE, suggest(hint = hint)?.category)
        }
    }

    @Test
    fun `productivity and news are suggested as Study`() {
        assertEquals(CategoryNames.STUDY, suggest(hint = AppCategoryHint.PRODUCTIVITY)?.category)
        assertEquals(CategoryNames.STUDY, suggest(hint = AppCategoryHint.NEWS)?.category)
    }

    @Test
    fun `maps is suggested as Life`() {
        assertEquals(CategoryNames.LIFE, suggest(hint = AppCategoryHint.MAPS)?.category)
    }

    @Test
    fun `an image app is ambiguous and gets no suggestion`() {
        // A camera or gallery app could be study material or pure waste; guessing would be
        // an invitation to accept the wrong one with a single tap.
        assertNull(suggest(hint = AppCategoryHint.IMAGE))
    }

    @Test
    fun `an app with no declared category gets no suggestion`() {
        assertNull(suggest(hint = AppCategoryHint.UNDEFINED))
    }

    @Test
    fun `phone messaging and system surfaces are suggested as Life`() {
        assertEquals(
            CategoryNames.LIFE,
            suggest(pkg = "com.example.dialer", hint = AppCategoryHint.UNDEFINED, isLifeSystemApp = true)?.category
        )
    }

    // --- earning apps are never suggested ---------------------------------------

    @Test
    fun `the owners offerwall apps are never suggested even when they look like video`() {
        listOf(
            "com.example.prorewards",
            "com.example.offerpro",
            "net.example.OfferPro.android"
        ).forEach { pkg ->
            assertNull(pkg, suggest(pkg = pkg, hint = AppCategoryHint.VIDEO))
            assertNull(pkg, suggest(pkg = pkg, hint = AppCategoryHint.UNDEFINED))
        }
    }

    @Test
    fun `an app with logged earnings is never suggested`() {
        assertNull(suggest(hint = AppCategoryHint.SOCIAL, isKnownEarningApp = true))
    }

    @Test
    fun `earning status beats the system-life rule`() {
        assertNull(
            suggest(
                pkg = "com.example.prorewards",
                hint = AppCategoryHint.UNDEFINED,
                isLifeSystemApp = true
            )
        )
    }

    // --- planning: what the owner actually sees ---------------------------------

    private fun input(
        pkg: String,
        label: String,
        hint: AppCategoryHint,
        category: String = CategoryNames.UNCLASSIFIED,
        life: Boolean = false,
        earning: Boolean = false,
        dismissed: Boolean = false
    ) = SuggestionInput(pkg, label, category, hint, life, earning, dismissed)

    @Test
    fun `an existing assignment is never re-suggested`() {
        val plan = CategorySuggestionRules.plan(
            listOf(
                input("com.example.reader", "Reader", AppCategoryHint.PRODUCTIVITY, category = CategoryNames.STUDY),
                input("com.example.video", "Video", AppCategoryHint.VIDEO)
            )
        )
        assertEquals(listOf("com.example.video"), plan.map { it.packageName })
    }

    @Test
    fun `a dismissed suggestion does not come back`() {
        val plan = CategorySuggestionRules.plan(
            listOf(
                input("com.example.chat", "Chat", AppCategoryHint.SOCIAL, dismissed = true),
                input("com.example.newsapp", "News", AppCategoryHint.NEWS)
            )
        )
        assertEquals(listOf("com.example.newsapp"), plan.map { it.packageName })
    }

    @Test
    fun `unclassifiable apps simply do not appear`() {
        val plan = CategorySuggestionRules.plan(
            listOf(
                input("com.example.tools", "Tools", AppCategoryHint.UNDEFINED),
                input("com.example.camera", "Camera", AppCategoryHint.IMAGE)
            )
        )
        assertTrue(plan.isEmpty())
    }

    @Test
    fun `a package is never listed twice`() {
        val plan = CategorySuggestionRules.plan(
            listOf(
                input("com.example.video", "Video", AppCategoryHint.VIDEO),
                input("com.example.video", "Video", AppCategoryHint.VIDEO)
            )
        )
        assertEquals(1, plan.size)
    }

    @Test
    fun `suggestions are ordered by label and carry their reason`() {
        val plan = CategorySuggestionRules.plan(
            listOf(
                input("com.example.video", "Zebra Video", AppCategoryHint.VIDEO),
                input("com.example.chat", "alpha chat", AppCategoryHint.SOCIAL)
            )
        )
        assertEquals(listOf("alpha chat", "Zebra Video"), plan.map { it.label })
        assertEquals(CategoryNames.WASTE, plan[0].category)
        assertEquals("social app", plan[0].reason)
    }

    @Test
    fun `the planner only ever proposes the three confirmed categories`() {
        val hints = AppCategoryHint.entries
        val plan = CategorySuggestionRules.plan(
            hints.mapIndexed { i, hint -> input("com.example.app$i", "App $i", hint) }
        )
        val allowed = setOf(CategoryNames.LIFE, CategoryNames.WASTE, CategoryNames.STUDY)
        assertTrue(plan.all { it.category in allowed })
        // Never "Unclassified" (a suggestion must say something), never "Earning" or
        // "College" (not derivable from a store category).
        assertTrue(plan.none { it.category == CategoryNames.UNCLASSIFIED })
    }
}
