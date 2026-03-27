import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.detekt)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.kotlinxSerialization)
    alias(libs.plugins.sqldelight)
}

kotlin {
    explicitApi()

    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64(),
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(libs.kotlinx.serialization.json)
            implementation(libs.kotlinx.datetime)
            implementation(libs.sqldelight.runtime)
            implementation(libs.sqldelight.coroutines)
            implementation(libs.decompose)
            implementation(libs.decompose.extensionsCompose)
            implementation(libs.essenty.lifecycle)
            implementation(libs.essenty.lifecycle.coroutines)
            implementation(libs.napier)
            implementation(libs.material3.adaptive)
            implementation(libs.material3.adaptive.layout)
            implementation(libs.material3.adaptive.navigation)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
            implementation(libs.kotlinx.coroutines.test)
        }
        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation(libs.sqldelight.jvmDriver)
        }
        val jvmTest by getting {
            dependencies {
                implementation(libs.sqldelight.jvmDriver)
            }
        }
        jvmTest.dependencies {
            implementation(libs.kotlin.testJunit)
            implementation(libs.junit)
            implementation(libs.compose.uitest)
        }
    }
}

// kotlinx-datetime 0.7.1 is pulled transitively by Compose 1.10 but the project
// is compiled against 0.6.0. Force all runtime configurations to stay on 0.6.0 so
// compile and runtime use the same jar and `kotlinx/datetime/Instant.class` is present.
configurations.all {
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime:${libs.versions.kotlinx.datetime.get()}")
    resolutionStrategy.force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:${libs.versions.kotlinx.datetime.get()}")
}

android {
    namespace = "dev.androidbroadcast.claudex"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "dev.androidbroadcast.claudex"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

detekt {
    source.setFrom(
        "src/commonMain/kotlin",
        "src/androidMain/kotlin",
        "src/iosMain/kotlin",
        "src/jvmMain/kotlin",
    )
    config.setFrom(rootProject.files("config/detekt/detekt.yml"))
    buildUponDefaultConfig = true
    allRules = false
}

tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    jvmTarget = "17"
    languageVersion = "1.9"
}

// Exclude Compose-generated sources from ktlint checks.
// The jlleitschuh ktlint plugin (12.x) adds generated KMP resource files to its
// source sets via lazy ConfigurableFileCollection providers, bypassing the
// standard filter{} API. We intercept at the task's sourceFiles field and
// replace it with a FileTree rooted at src/ only.
tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().configureEach {
    setSource(fileTree("src") { include("**/*.kt", "**/*.kts") })
}
// Re-apply after all KMP sources have been wired so the src-only tree wins.
gradle.taskGraph.whenReady {
    tasks.withType<org.jlleitschuh.gradle.ktlint.tasks.BaseKtLintCheckTask>().forEach { task ->
        task.setSource(fileTree("src") { include("**/*.kt", "**/*.kts") })
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "dev.androidbroadcast.claudex.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "dev.androidbroadcast.claudex"
            packageVersion = "1.0.0"
        }
    }
}

sqldelight {
    databases {
        create("ClaudexDatabase") {
            packageName.set("dev.androidbroadcast.claudex.data.db")
            schemaOutputDirectory.set(file("src/commonMain/sqldelight/schema"))
            migrationOutputDirectory.set(file("src/commonMain/sqldelight/migrations"))
            verifyMigrations.set(true)
        }
    }
}
