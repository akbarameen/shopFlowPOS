import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    // alias(libs.plugins.composeHotReload) // Disabled due to "generateFunctionKeyMetaAnnotations" error
    alias(libs.plugins.kotlinSerialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }

    jvm()

    sourceSets {
        commonMain.dependencies {
            // Compose
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material3)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            
            // Lifecycle
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            
            // Navigation
            implementation(libs.navigation.compose)
            
            // Koin
            implementation(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
            
            // Coroutines
            implementation(libs.kotlinx.coroutines.core)
            
            // DateTime
            implementation(libs.kotlinx.datetime)

            // ─── Core Modules ───────────────────────────────────────────────
            implementation(projects.core.coreModel)
            implementation(projects.core.coreCommon)
            implementation(projects.core.coreDatabase)
            implementation(projects.core.coreNetwork)
            implementation(projects.core.coreUi)
            
            implementation(libs.ktor.client.core)
            
            // ─── Feature Modules ────────────────────────────────────────────
            implementation(projects.feature.featureAuth)
            implementation(projects.feature.featureDashboard)
            implementation(projects.feature.featurePos)
            implementation(projects.feature.featureInventory)
            implementation(projects.feature.featureTransactions)
            implementation(projects.feature.featureSalesReturn)
            implementation(projects.feature.featureCustomers)
            implementation(projects.feature.featureSuppliers)
            implementation(projects.feature.featureExpenses)
            implementation(projects.feature.featureLedger)
            implementation(projects.feature.featureReports)
            implementation(projects.feature.featureInstallments)
            implementation(projects.feature.featureRepairs)
            implementation(projects.feature.featureSettings)
            implementation(projects.feature.featurePurchase)
            implementation(projects.feature.featureDues)

            implementation(libs.compose.icons.extended)
            implementation(libs.compose.icons.core)
        }

        androidMain.dependencies {
            implementation(libs.androidx.activity.compose)
            implementation(libs.koin.android)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
        }
    }
}

android {
    namespace = "com.matechmatrix.shopflowpos"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.matechmatrix.shopflowpos"
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

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.matechmatrix.shopflowpos.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.matechmatrix.shopflowpos"
            packageVersion = "1.0.0"
        }
    }
}
