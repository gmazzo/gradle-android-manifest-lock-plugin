![GitHub](https://img.shields.io/github/license/gmazzo/gradle-android-manifest-lock-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.android.manifest.lock)](https://plugins.gradle.org/plugin/io.github.gmazzo.android.manifest.lock)
[![Build Status](https://github.com/gmazzo/gradle-android-manifest-lock-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/gmazzo/gradle-android-manifest-lock-plugin/actions/workflows/build.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-android-manifest-lock-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-android-manifest-lock-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.test.aggregation+-repo:github.com/gmazzo/gradle-android-manifest-lock-plugin)

# gradle-android-manifest-lock-plugin
A gradle Gradle to control what Permissions, SDK-level, and other PlayStore listing sensitive settings is added into the Android Manifest

# Usage
Apply the plugin at the **root** project:
```kotlin
plugins {
    id("io.github.gmazzo.android.manifest.lock") version "<latest>" 
}
```
