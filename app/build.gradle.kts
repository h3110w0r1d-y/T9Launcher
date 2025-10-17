import com.android.build.api.variant.FilterConfiguration
import com.android.build.api.variant.impl.VariantOutputImpl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

var isBundleTask = false
gradle.startParameter.taskNames.forEach { taskName ->
    if (taskName.contains("bundle")) {
        isBundleTask = true
    }
}

android {
    namespace = "com.h3110w0r1d.t9launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.h3110w0r1d.t9launcher"
        minSdk = 26
        targetSdk = 36
        versionCode = 36
        versionName = "1.7.11"
        vectorDrawables {
            useSupportLibrary = true
        }
    }
    splits {
        abi {
            isEnable = !isBundleTask
            isUniversalApk = true
            reset()
            include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            @Suppress("UnstableApiUsage")
            vcsInfo.include = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"))
            packaging {
                resources {
                    excludes += "META-INF/androidx/**"
                    excludes += "META-INF/*.version"
                    excludes += "META-INF/*.md"
                    excludes += "DebugProbesKt.bin"
                    excludes += "kotlin-tooling-metadata.json"
                    excludes += "kotlin/**"
                }
            }
        }
    }

    androidComponents.onVariants { variant ->
        var currentVersionName = defaultConfig.versionName

        variant.outputs.forEach { output ->
            output.versionName.set(currentVersionName)
            if (output is VariantOutputImpl) {
                val abi =
                    output
                        .getFilter(FilterConfiguration.FilterType.ABI)
                        ?.identifier ?: "universal"
                val apkName = "T9Launcher-$currentVersionName-$abi-${variant.buildType}.apk"
                output.outputFileName.set(apkName)
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_21)
        }
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-Xannotation-default-target=param-property")
        }
    }
}

dependencies {
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.datastore:datastore-preferences-core:1.1.7")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")
    implementation("androidx.navigation:navigation-compose:2.9.5")

    implementation(platform("androidx.compose:compose-bom:2025.10.00"))
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")

    implementation("com.google.dagger:hilt-android:2.57.2")
    kapt("com.google.dagger:hilt-compiler:2.57.2")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
}
