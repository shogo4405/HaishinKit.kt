import org.jetbrains.dokka.gradle.DokkaTaskPartial

// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.jetbrainsKotlinAndroid) apply false
    alias(libs.plugins.androidLibrary) apply false
    id("org.jetbrains.dokka") version "1.9.20" apply true
    id("org.jlleitschuh.gradle.ktlint") version "11.6.1" apply true
}

subprojects {
    apply(plugin = "org.jlleitschuh.gradle.ktlint")

    tasks.withType<DokkaTaskPartial>().configureEach {
        dokkaSourceSets.configureEach {
            val moduleDocsFile = "module-docs.md"
            if (file(moduleDocsFile).exists()) {
                includes.from(moduleDocsFile)
            }
        }
    }
}

tasks.dokkaHtmlMultiModule.configure {
    outputDirectory.set(File("docs"))
    includes.from("README.md")
}
