plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.plugin.compose")
    id("com.google.dagger.hilt.android")
    id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.h3110w0r1d.t9launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.h3110w0r1d.t9launcher"
        minSdk = 26
        targetSdk = 36
        versionCode = 26
        versionName = "1.7.1"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    flavorDimensions += "version"
    productFlavors {
        create("default") {}
        create("xposed") {}
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            @Suppress("UnstableApiUsage")
            vcsInfo.include = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            splits {
                abi {
                    isEnable = true
                    isUniversalApk = true
                    reset()
                    include("armeabi-v7a", "arm64-v8a", "x86", "x86_64")
                }
            }
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
    androidComponents {
        onVariants { variant ->
            var currentVersionName = defaultConfig.versionName
            if (variant.name.contains("xposed", true)) {
                currentVersionName += "-xposed"
            }
            variant.outputs.forEach { output ->
                output.versionName.set(currentVersionName)
                if (output is com.android.build.api.variant.impl.VariantOutputImpl) {
                    val abi =
                        output
                            .getFilter(
                                com.android.build.api.variant.FilterConfiguration.FilterType.ABI,
                            )?.identifier ?: "universal"
                    val apkName = "T9Launcher-$currentVersionName-$abi-${variant.buildType}.apk"
                    output.outputFileName.set(apkName)
                }
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlin {
        compilerOptions {
            jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_1_8)
        }
    }
    buildFeatures {
        compose = true
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
        compilerOptions {
            freeCompilerArgs.add("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
        }
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.navigation:navigation-compose:2.9.4")
    implementation("androidx.activity:activity-compose:1.11.0")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.4")

    implementation(platform("androidx.compose:compose-bom:2025.09.00"))
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-text")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.1")

    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.9.0")
    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.3.0")

    compileOnly(files("libs/api-82.jar"))
}
