package io.github.pabi277.dayledger.data

/**
 * Packages whose time Android's Digital Wellbeing does **not** count as screen time:
 * the home screen, the system UI, and the phone/dialer surface.
 *
 * Owner decision, 2026-10-23: Day Ledger excludes these from Today's total and from the
 * app list, so that kill test P0 compares like with like instead of being explained away.
 * On the owner's vivo 1820 that was worth 28 minutes of a 286-minute reading —
 * `com.sec.android.app.launcher` 20 m plus `com.samsung.android.incallui` 8 m, neither of
 * which appears in Digital Wellbeing at all.
 *
 * Two layers, because neither is sufficient alone:
 *
 * - [ALWAYS_EXCLUDED] covers the well-known OEM packages by name.
 * - [isExcluded] also takes the device's *resolved* home package and *resolved* default
 *   dialer package, because a launcher can be a Play Store app with an arbitrary package
 *   name. Both are read from the platform with no new permission
 *   (`PackageManager.resolveActivity` for `CATEGORY_HOME`, `TelecomManager.defaultDialerPackage`).
 *
 * Deliberately **not** a suffix rule. `endsWith(".launcher")` would also swallow a
 * third-party app, and nothing in this matcher should guess.
 */
object SystemPackageRules {

    val ALWAYS_EXCLUDED: Set<String> = setOf(
        // System UI
        "com.android.systemui",

        // Home screens
        "com.android.launcher",
        "com.android.launcher3",
        "com.google.android.apps.nexuslauncher",
        "com.sec.android.app.launcher",
        "com.miui.home",
        "com.bbk.launcher2",
        "com.vivo.launcher",
        "com.oppo.launcher",
        "com.transsion.XOSLauncher",

        // Phone / dialer surfaces
        "com.android.dialer",
        "com.google.android.dialer",
        "com.android.incallui",
        "com.samsung.android.incallui",
        "com.android.server.telecom"
    )

    fun isExcluded(
        packageName: String,
        homePackage: String? = null,
        dialerPackage: String? = null
    ): Boolean =
        packageName in ALWAYS_EXCLUDED ||
            (homePackage != null && packageName == homePackage) ||
            (dialerPackage != null && packageName == dialerPackage)

    fun exclusion(homePackage: String? = null, dialerPackage: String? = null): (String) -> Boolean =
        { packageName -> isExcluded(packageName, homePackage, dialerPackage) }
}
