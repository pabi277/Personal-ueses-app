package io.github.pabi277.dayledger.data

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Owner decision 2026-10-23: Today's total and app list exclude the home screen, the
 * system UI and the dialer, so kill test P0 compares like with like against Digital
 * Wellbeing. All package names below are fictional.
 */
class SystemPackageRulesTest {

    @Test
    fun `the owner device's launcher and dialer are excluded by name`() {
        assertTrue(SystemPackageRules.isExcluded("com.sec.android.app.launcher"))
        assertTrue(SystemPackageRules.isExcluded("com.samsung.android.incallui"))
    }

    @Test
    fun `system ui and the aosp dialer are excluded`() {
        assertTrue(SystemPackageRules.isExcluded("com.android.systemui"))
        assertTrue(SystemPackageRules.isExcluded("com.android.dialer"))
        assertTrue(SystemPackageRules.isExcluded("com.google.android.dialer"))
    }

    @Test
    fun `a launcher resolved from the platform is excluded whatever it is called`() {
        assertTrue(
            SystemPackageRules.isExcluded(
                "com.studio.customhome",
                homePackage = "com.studio.customhome"
            )
        )
    }

    @Test
    fun `a dialer resolved from the platform is excluded whatever it is called`() {
        assertTrue(
            SystemPackageRules.isExcluded(
                "com.telco.softphone",
                dialerPackage = "com.telco.softphone"
            )
        )
    }

    @Test
    fun `ordinary apps are never excluded`() {
        listOf(
            "com.example.chat",
            "com.example.reader",
            "com.example.notes",
            "com.example.video"
        ).forEach { pkg ->
            assertFalse(pkg, SystemPackageRules.isExcluded(pkg, "com.example.launcher", "com.example.dialer"))
        }
    }

    @Test
    fun `the matcher does not guess from a name suffix`() {
        // A third-party app that merely mentions "launcher" must not be swallowed: this is
        // why the rule is an exact set plus the platform-resolved home package.
        assertFalse(SystemPackageRules.isExcluded("com.example.launcherdemo"))
        assertFalse(SystemPackageRules.isExcluded("com.example.chat.launcher"))
    }

    @Test
    fun `the exclusion predicate matches the single-package check`() {
        val rule = SystemPackageRules.exclusion("com.example.launcher", "com.example.dialer")
        assertTrue(rule("com.example.launcher"))
        assertTrue(rule("com.example.dialer"))
        assertTrue(rule("com.android.systemui"))
        assertFalse(rule("com.example.chat"))
    }
}
