import java.io.File

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.devtools.ksp")
}

// One stable, owner-controlled sideload identity.
//
// The owner creates the keystore OUTSIDE this repository and stores it in the
// four GitHub Actions secrets named below; CI decodes it to a temporary file and
// exports ANDROID_KEYSTORE_PATH. When all four are present, BOTH the debug
// `app-progress-apk` and the signed release are signed with that one key, so every
// later APK installs as an update over the previous one. No value is ever read
// from, written to, or committed in this repository.
//
// Without the secrets the build still runs (CI must always upload a progress APK)
// but falls back to AGP's throwaway debug key, which cannot be installed over an
// owner-key build; the workflow warns loudly in that case.
val ownerKeystorePath: String? = System.getenv("ANDROID_KEYSTORE_PATH")
val ownerKeystorePassword: String? = System.getenv("ANDROID_KEYSTORE_PASSWORD")
val ownerKeyAlias: String? = System.getenv("ANDROID_KEY_ALIAS")
val ownerKeyPassword: String? = System.getenv("ANDROID_KEY_PASSWORD")

val ownerKeystoreFile = ownerKeystorePath?.let { File(it) }
val hasOwnerSigning: Boolean =
    ownerKeystoreFile?.isFile == true &&
        !ownerKeystorePassword.isNullOrBlank() &&
        !ownerKeyAlias.isNullOrBlank() &&
        !ownerKeyPassword.isNullOrBlank()

android {
    namespace = "io.github.pabi277.dayledger"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.github.pabi277.dayledger"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        // Keep a v1 (JAR) signature on the progress APK as well as v2. With
        // minSdk 26 AGP would otherwise emit v2 only, which is fine for installs
        // but leaves nothing for `keytool -printcert -jarfile` to read — and the
        // certificate fingerprint is the evidence that signing is stable.
        getByName("debug") {
            enableV1Signing = true
            enableV2Signing = true
        }
        if (hasOwnerSigning) {
            create("owner") {
                storeFile = ownerKeystoreFile
                storePassword = ownerKeystorePassword
                keyAlias = ownerKeyAlias
                keyPassword = ownerKeyPassword
                // The progress APK is a debug variant that the owner sideloads; the
                // whole point of this config is that it keeps ONE certificate.
                enableV1Signing = true
                enableV2Signing = true
            }
        }
    }

    buildTypes {
        debug {
            if (hasOwnerSigning) {
                signingConfig = signingConfigs.getByName("owner")
            }
        }
        release {
            if (hasOwnerSigning) {
                signingConfig = signingConfigs.getByName("owner")
            }
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        compose = true
    }

}

// Room schema export (Phase 7 part 7.1). The exported JSON is the baseline that
// future schema versions must migrate from, and the JVM migration tests compare
// the migrated tables against it.
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.7")
    implementation("androidx.activity:activity-compose:1.9.3")

    implementation(platform("androidx.compose:compose-bom:2024.12.01"))
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")

    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    implementation("androidx.navigation:navigation-compose:2.8.5")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.7")
    implementation("androidx.work:work-runtime-ktx:2.10.0")
    implementation("androidx.glance:glance-appwidget:1.1.1")
    implementation("androidx.glance:glance-material3:1.1.1")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.9.0")

    testImplementation("junit:junit:4.13.2")
    // Real SQLite on the JVM so migrations and the restore planner are tested
    // against actual SQL rather than mocks.
    testImplementation("org.xerial:sqlite-jdbc:3.46.1.3")
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation(platform("androidx.compose:compose-bom:2024.12.01"))
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
