pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
    }
}

dependencyResolutionManagement {
    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS
    repositories {
        mavenCentral()
        google()
    }
    versionCatalogs {
        create("demoLibs") {
            from(files("gradle/demo.libs.versions.toml"))
        }
    }
}

rootProject.name = "gradle-android-manifest-lock-plugin"

includeBuild("plugin")
include("demo")
