pluginManagement {
    repositories {
        google {
            content {
                includeGroupByRegex("com\\.android.*")
                includeGroupByRegex("com\\.google.*")
                includeGroupByRegex("androidx.*")
            }
        }
        mavenCentral()
        gradlePluginPortal()
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        google()
        mavenCentral()
    }
}

rootProject.name = "Offread"
include(":app")
include(":core:entity")
include(":core:domain")
include(":core:ui")
include(":core:datastore")
include(":core:database")
include(":onboarding:domain")
include(":onboarding:data")
include(":onboarding:presentation")
include(":library:domain")
include(":library:data")
include(":library:presentation")
include(":importer:domain")
include(":importer:data")
include(":importer:presentation")
include(":reader:domain")
include(":reader:data")
include(":reader:presentation")
include(":terms:domain")
include(":terms:data")
include(":terms:presentation")
 