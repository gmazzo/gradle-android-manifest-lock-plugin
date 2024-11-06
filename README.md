![GitHub](https://img.shields.io/github/license/gmazzo/gradle-android-manifest-lock-plugin)
[![Gradle Plugin Portal](https://img.shields.io/gradle-plugin-portal/v/io.github.gmazzo.android.manifest.lock)](https://plugins.gradle.org/plugin/io.github.gmazzo.android.manifest.lock)
[![Build Status](https://github.com/gmazzo/gradle-android-manifest-lock-plugin/actions/workflows/build.yaml/badge.svg)](https://github.com/gmazzo/gradle-android-manifest-lock-plugin/actions/workflows/build.yaml)
[![Coverage](https://codecov.io/gh/gmazzo/gradle-android-manifest-lock-plugin/branch/main/graph/badge.svg?token=D5cDiPWvcS)](https://codecov.io/gh/gmazzo/gradle-android-manifest-lock-plugin)
[![Users](https://img.shields.io/badge/users_by-Sourcegraph-purple)](https://sourcegraph.com/search?q=content:io.github.gmazzo.android.manifest.lock+-repo:github.com/gmazzo/gradle-android-manifest-lock-plugin)

# gradle-android-manifest-lock-plugin
A gradle Gradle to control what Permissions, SDK-level, and other PlayStore listing-sensitive settings is added into the Android Manifest

# Usage
Apply the plugin at the any Android (application or library) module:
```kotlin
plugins {
    id("io.github.gmazzo.android.manifest.lock") version "<latest>" 
}
```
The `androidManifestLock` task will be added to the build, and automatically bind to `check`.
When run, an `src/main/AndroidManifest.lock` (default location) file will be created with a content similar to:
```yaml
main:
  namespace: io.github.gmazzo.android.manifest.lock.demo
  minSDK: 24
  targetSDK: 34
  permissions:
    - android.permission.ACCESS_NETWORK_STATE
    - android.permission.FOREGROUND_SERVICE
    - android.permission.RECEIVE_BOOT_COMPLETED
    - android.permission.WAKE_LOCK
    - io.github.gmazzo.android.manifest.lock.demo.DYNAMIC_RECEIVER_NOT_EXPORTED_PERMISSION
  features:
    - glEsVersion: 0x00020000
      required: true
  libraries:
    - org.apache.http.legacy:
        required: false
  nativeLibraries:
    - libnative-lib.so:
        requiredBy: manifest
        required: false
    - libimage_processing_util_jni:
        requiredBy: androidx.camera:camera-core
        abis:
          - arm64-v8a
          - armeabi-v7a
          - x86
          - x86_64
  exports:
    activity:
      - io.github.gmazzo.android.manifest.lock.demo.MainActivity
    service:
      - androidx.work.impl.background.systemjob.SystemJobService
    receiver:
      - androidx.work.impl.diagnostics.DiagnosticsReceiver
variants:
  debug:
    configurations:
      - reqFiveWayNav: true
        reqNavigation: trackball
    permissions:
      - android.permission.POST_NOTIFICATIONS
      - android.permission.READ_EXTERNAL_STORAGE
      - android.permission.WRITE_EXTERNAL_STORAGE
    exports:
      activity:
        - leakcanary.internal.activity.LeakActivity
      activity-alias:
        - leakcanary.internal.activity.LeakLauncherActivity
  release:
    configurations:
      - reqFiveWayNav: true
        reqKeyboardType: qwerty
    permissions:
      - android.permission.INTERNET
    nativeLibraries:
      - libcrashlytics:
          requiredBy: com.google.firebase:firebase-crashlytics-ndk
          abis:
            - arm64-v8a
            - armeabi-v7a
            - x86
            - x86_64
      - libcrashlytics-common:
          requiredBy: com.google.firebase:firebase-crashlytics-ndk
          abis:
            - arm64-v8a
            - armeabi-v7a
            - x86
            - x86_64
      - libcrashlytics-handler:
          requiredBy: com.google.firebase:firebase-crashlytics-ndk
          abis:
            - arm64-v8a
            - armeabi-v7a
            - x86
            - x86_64
      - libcrashlytics-trampoline:
          requiredBy: com.google.firebase:firebase-crashlytics-ndk
          abis:
            - arm64-v8a
            - armeabi-v7a
            - x86
            - x86_64
fingerprint: 3ea645556e4cce4a3eebbb7543291eec
```
You can later commit this file to keep track and detect unnoticed changes (by introducing/bumping a 3rd party dependency for instance).

## Configuration

### Changing the location of the lock file
```kotlin
android {
    manifestLock {
        lockFile = layout.projectDirectory.file("android.lock")
    }
}
```

### Customizing the content of the lock file
There are many configuration options to customize the content of the lock file, such as: `sdkVersion`, `permissions`, `features`, `libraries` and `exports`.

For instance, to remove the `exports` section from the lock file, you can set the `exports` property to `false`:
```kotlin
android {
    manifestLock {
        content {
            exports = false
        }
    }
}
```

### Failing if lock has changes on CI
```kotlin
android {
    manifestLock {
        failOnLockChange = providers.environmentVariable("CI").map { it.toBoolean() }.orElse(false)
    }
}
```
