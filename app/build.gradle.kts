plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.dagger.hilt)
    alias(libs.plugins.kotlin.kapt)
}

android {
    namespace = "com.tomer.myflix"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.tomer.myflix"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
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
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.work.runtime.ktx)


    implementation(libs.retrofit)
    implementation(libs.okttp)
    implementation(libs.retrofit.gson)

    implementation(libs.coil.compose)
    implementation(libs.gson)

    implementation(libs.exo.player)
    implementation(libs.exo.player.ui)
    implementation(libs.exo.player.hls)
    implementation(libs.exo.player.http)

    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.palette.ktx)
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    implementation("com.github.Dimezis:BlurView:version-3.1.0")

    //Dagger-Hilt
    implementation(libs.dagger.hilt.android)
    implementation(libs.dagger.hilt.compose.navigation)
    kapt(libs.dagger.hilt.compiler)

    //Room
    implementation(libs.room)
    kapt(libs.room.compiler)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.tooling)
    debugImplementation(libs.androidx.ui.test.manifest)
}
tasks.register<Copy>("copyApk") {

    val apkFile = layout.buildDirectory.file("outputs/apk/release/app-release.apk")
    val destDir = file("/home/tom/apks")
    destDir.mkdirs()
    from(apkFile)
    into(destDir)
    rename("app-release.apk", "${rootProject.name}.apk")
}

tasks.whenTaskAdded { if (name == "assembleRelease") { finalizedBy("copyApk") } }