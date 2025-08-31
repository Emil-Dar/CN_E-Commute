pluginManagement {
    repositories {
        gradlePluginPortal()
        google()
        mavenCentral()
        // Add JitPack as a last resort for stubborn dependencies
        maven("https://jitpack.io")
    }
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
        // Add JitPack as a last resort for stubborn dependencies
        maven("https://jitpack.io")
    }
}
rootProject.name = "CN E-Commute"
include(":app")
