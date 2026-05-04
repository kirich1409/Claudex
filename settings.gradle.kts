rootProject.name = "Claudex"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositories {
        google {
            mavenContent {
                includeGroupAndSubgroups("androidx")
                includeGroupAndSubgroups("com.android")
                includeGroupAndSubgroups("com.google")
            }
        }
        mavenCentral()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.danilopianini.gradle-pre-commit-git-hooks") version "2.1.16"
}

gitHooks {
    preCommit {
        from { "./gradlew ktlintFormat" }
    }
    commitMsg { conventionalCommits() }
    hook("pre-push") {
        from {
            """
            ./gradlew ktlintCheck &&
            ./gradlew detekt &&
            ./gradlew :composeApp:jvmTest
            """.trimIndent()
        }
    }
    createHooks(overwriteExisting = true)
}

include(":composeApp")
