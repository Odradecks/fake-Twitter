plugins {
    alias(libs.plugins.android.application)
    id("com.google.gms.google-services") // 在 app 的 build.gradle 中应用插件
}

android {
    namespace = "com.example.twitter"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.twitter"
        minSdk = 24
        targetSdk = 34
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)

    // Firebase and Google Play services BoM
    implementation(platform("com.google.firebase:firebase-bom:33.5.1"))  // 这里有更新
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    // Google Play services
    implementation("com.google.android.gms:play-services-auth:21.2.0")
    implementation(libs.play.services.base)

    // 测试依赖项
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)

    implementation("com.google.android.material:material:1.9.0") // 确保使用最新版本
    implementation("de.hdodenhof:circleimageview:3.1.0")


    // Glide 依赖
    implementation("com.github.bumptech.glide:glide:4.15.1") // Glide 的最新版本

    // Glide 编译器
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    implementation("com.squareup.picasso:picasso:2.71828")
    implementation("de.hdodenhof:circleimageview:3.1.0")






}
