pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
    // libs.versions.toml dosyası projede zaten mevcutsa from metodu kullanmayın
    // versionCatalogs bloğunu tamamen kaldırın
}

rootProject.name = "FizyoApp"
include(":app")