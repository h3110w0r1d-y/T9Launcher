plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.h3110w0r1d.t9launcher"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.h3110w0r1d.t9launcher"
        minSdk = 26
        targetSdk = 36
        versionCode = 9
        versionName = "1.3.1"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                    getDefaultProguardFile("proguard-android-optimize.txt"),
                    "proguard-rules.pro"
            )
        }
    }

    applicationVariants.all {
        val variant = this
        if (variant.buildType.name == "release")
            variant.outputs.map { it as com.android.build.gradle.internal.api.BaseVariantOutputImpl }
                    .forEach { output ->
                        val abi = output.getFilter(com.android.build.OutputFile.ABI) ?: "universal"
                        val apkName = "T9Launcher-${defaultConfig.versionName}-${abi}-${variant.buildType.name}.apk"
                        output.outputFileName = apkName
                    }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    //    kotlinOptions {
//        jvmTarget = "11"
//    }
}

dependencies {
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("androidx.databinding:databinding-runtime:8.12.2")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("androidx.swiperefreshlayout:swiperefreshlayout:1.1.0")
    implementation("com.google.code.gson:gson:2.13.1")
    implementation("com.belerweb:pinyin4j:2.5.1")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.core:core-ktx:1.17.0")
}