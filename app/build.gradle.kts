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
        versionCode = 11
        versionName = "1.5.0"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
//            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    applicationVariants.all {
        val variant = this
        if (variant.buildType.name == "release") {
            variant.outputs
                .map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                .forEach { output ->
                    val abi = output.getFilter(com.android.build.OutputFile.ABI) ?: "universal"
                    val apkName = "T9Launcher-${defaultConfig.versionName}-$abi-${variant.buildType.name}.apk"
                    output.outputFileName = apkName
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

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"
    }
    kotlinOptions {
        freeCompilerArgs = listOf("-XXLanguage:+PropertyParamAnnotationDefaultTargetMode")
    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.databinding:databinding-runtime:8.13.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.belerweb:pinyin4j:2.5.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.compose.material3:material3:1.3.2")
    implementation("androidx.compose.foundation:foundation:1.9.0")

    // Jetpack Compose BOM
    val composeBomVersion = "2025.08.01"
    implementation(platform("androidx.compose:compose-bom:$composeBomVersion"))
    androidTestImplementation(platform("androidx.compose:compose-bom:$composeBomVersion"))

    // Jetpack Compose dependencies
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.navigation:navigation-compose:2.9.3")
    implementation("androidx.activity:activity-compose:1.10.1")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.9.3")

    implementation("com.google.dagger:hilt-android:2.57.1")
    kapt("com.google.dagger:hilt-compiler:2.57.1")
    implementation("androidx.hilt:hilt-navigation-compose:1.2.0")

    implementation("io.coil-kt:coil-compose:2.7.0")
    implementation("androidx.compose.material:material-icons-extended:1.7.8")

    implementation("androidx.datastore:datastore-preferences:1.1.7")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.9.3")
}
