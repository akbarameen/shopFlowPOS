rootProject.name = "ShopFlowPOS"
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
        // ADD THIS:
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")

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
}

include(":composeApp")
// ─── Core Modules ────────────────────────────────────────────────────────────
include(":core:core-model")
include(":core:core-common")
include(":core:core-database")
include(":core:core-network")
include(":core:core-ui")

// ─── Feature Modules ─────────────────────────────────────────────────────────
include(":feature:feature-auth")
include(":feature:feature-dashboard")
include(":feature:feature-pos")
include(":feature:feature-inventory")
include(":feature:feature-transactions")
include(":feature:feature-sales-return")
include(":feature:feature-customers")
include(":feature:feature-suppliers")
include(":feature:feature-expenses")
include(":feature:feature-ledger")
include(":feature:feature-reports")
include(":feature:feature-installments")
include(":feature:feature-repairs")
include(":feature:feature-settings")
include(":feature:feature-purchase")
include(":feature:feature-dues")
include(":feature:feature-backup")
