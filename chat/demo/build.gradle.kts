buildscript {
    dependencies {
        classpath("org.jetbrains.dokka:dokka-gradle-plugin:1.8.20")
        val currentGradleVersion = GradleVersion.current()
        val androidGradlePluginVersion = when {
            currentGradleVersion >= GradleVersion.version("8.9") -> "8.7.0"
            currentGradleVersion >= GradleVersion.version("8.7") -> "8.6.1"
            currentGradleVersion >= GradleVersion.version("8.6") -> "8.4.2"
            currentGradleVersion >= GradleVersion.version("8.4") -> "8.3.2"
            currentGradleVersion >= GradleVersion.version("8.2") -> "8.2.2"
            currentGradleVersion >= GradleVersion.version("8.0") -> "8.1.4"
            currentGradleVersion >= GradleVersion.version("7.2") -> "7.0.0"
            else -> "4.2.0"
        }
        classpath("com.android.tools.build:gradle:$androidGradlePluginVersion")
    }
}

plugins {
    id("com.android.application") apply false
    id("org.jetbrains.kotlin.android") apply false
    id("com.android.library") apply false
}
