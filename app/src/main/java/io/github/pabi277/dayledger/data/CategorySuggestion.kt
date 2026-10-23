package io.github.pabi277.dayledger.data

/**
 * F1 / R2a — suggested default categories.
 *
 * Owner decision (2026-10-23), recorded verbatim in `docs/ROADMAP.md`: categories are
 * **suggestions the owner confirms with one tap**. Nothing is ever filled in silently.
 * An existing assignment is never overwritten and never re-suggested; an ambiguous or
 * unknown app gets **no** suggestion at all; earning apps are never suggested.
 *
 * The mapping uses only two inputs the device already provides without a new permission:
 * [AppCategoryHint] (from `ApplicationInfo.category`) and the system-life-app rule
 * (dialer, SMS, home and the system surfaces around them). Everything here is pure so the
 * mapping is unit-tested with fictional packages; the Android lookups live in
 * `UsageRepository`.
 */
enum class AppCategoryHint {
    UNDEFINED,
    GAME,
    AUDIO,
    VIDEO,
    IMAGE,
    SOCIAL,
    NEWS,
    MAPS,
    PRODUCTIVITY
}

/** Category display names, matching the seeded `categories.name` values. */
object CategoryNames {
    const val LIFE = "Life"
    const val WASTE = "Social/Waste"
    const val STUDY = "Study"
    const val UNCLASSIFIED = "Unclassified"
}

data class CategoryGuess(
    val category: String,
    val reason: String
)

/** One unclassified app, described well enough to decide on a suggestion. */
data class SuggestionInput(
    val packageName: String,
    val label: String,
    val category: String,
    val hint: AppCategoryHint,
    val isLifeSystemApp: Boolean = false,
    val isKnownEarningApp: Boolean = false,
    val dismissed: Boolean = false
)

data class AppCategorySuggestion(
    val packageName: String,
    val label: String,
    val category: String,
    val reason: String
)

object CategorySuggestionRules {

    /**
     * Earning apps must never be suggested. Two sources: the owner's named offerwall apps,
     * and the app's own earnings log — if the owner has ever logged rupees against a
     * package, that package is by definition an earning app.
     */
    private val EARNING_PACKAGE_MARKERS = listOf("prorewards", "offerpro")

    fun isEarningPackage(packageName: String): Boolean {
        val lower = packageName.lowercase()
        return EARNING_PACKAGE_MARKERS.any { lower.contains(it) }
    }

    /**
     * The single rule. Returns `null` when the app is ambiguous, unknown, or an earning
     * app — "no suggestion" is a first-class answer here, not a failure.
     */
    fun suggest(
        packageName: String,
        hint: AppCategoryHint,
        isLifeSystemApp: Boolean,
        isKnownEarningApp: Boolean = false
    ): CategoryGuess? {
        if (isKnownEarningApp || isEarningPackage(packageName)) return null
        if (isLifeSystemApp) {
            return CategoryGuess(CategoryNames.LIFE, "phone, messaging or system surface")
        }
        return when (hint) {
            AppCategoryHint.GAME -> CategoryGuess(CategoryNames.WASTE, "game")
            AppCategoryHint.VIDEO -> CategoryGuess(CategoryNames.WASTE, "video app")
            AppCategoryHint.SOCIAL -> CategoryGuess(CategoryNames.WASTE, "social app")
            AppCategoryHint.AUDIO -> CategoryGuess(CategoryNames.WASTE, "audio app")
            AppCategoryHint.PRODUCTIVITY -> CategoryGuess(CategoryNames.STUDY, "productivity app")
            AppCategoryHint.NEWS -> CategoryGuess(CategoryNames.STUDY, "news app")
            AppCategoryHint.MAPS -> CategoryGuess(CategoryNames.LIFE, "maps app")
            // A camera/gallery app could be study material or pure waste, and an app that
            // declares no category tells us nothing. Neither gets a guess.
            AppCategoryHint.IMAGE -> null
            AppCategoryHint.UNDEFINED -> null
        }
    }

    /**
     * The whole planning step, pure: who gets a suggestion, and with what.
     *
     * Never suggests for an app that already has an assignment (anything other than the
     * default `Unclassified`), never for one the owner dismissed, never twice for the same
     * package.
     */
    fun plan(inputs: List<SuggestionInput>): List<AppCategorySuggestion> =
        inputs.asSequence()
            .filter { it.category == CategoryNames.UNCLASSIFIED }
            .filterNot { it.dismissed }
            .distinctBy { it.packageName }
            .mapNotNull { input ->
                suggest(
                    packageName = input.packageName,
                    hint = input.hint,
                    isLifeSystemApp = input.isLifeSystemApp,
                    isKnownEarningApp = input.isKnownEarningApp
                )?.let { guess ->
                    AppCategorySuggestion(
                        packageName = input.packageName,
                        label = input.label,
                        category = guess.category,
                        reason = guess.reason
                    )
                }
            }
            .sortedBy { it.label.lowercase() }
            .toList()
}
