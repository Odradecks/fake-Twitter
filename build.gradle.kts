// 顶级 build.gradle 文件
buildscript {
    repositories {
        google()
        mavenCentral()
    }
    dependencies {
        classpath("com.android.tools.build:gradle:8.0.2")
        classpath("com.google.gms:google-services:4.4.2")
    }
}

// plugins 块中不再声明 google-services
plugins {
    alias(libs.plugins.android.application) apply false
}
