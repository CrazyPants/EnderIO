import com.github.spotbugs.snom.Effort
import com.github.spotbugs.snom.SpotBugsTask
import java.net.URI

plugins {
    id("maven-publish")
    id("net.neoforged.moddev") version "1.0.19" apply false
    id("com.diffplug.spotless") version "6.25.0"
    id("idea")
    id("com.github.spotbugs") version "6.0.22"
}

println("Release type: ${getReleaseType()}")

idea {
    module {
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

allprojects {
    gradle.projectsEvaluated {
        tasks.withType<JavaCompile> {
            options.compilerArgs.addAll(arrayOf("-Xmaxerrs", "400"))
        }
    }
}

// Normally we'd do this in the shared buildSrc file
// However, we need this context before loading subprojects or stuff explodes.
subprojects {
    if (project.name != "ensure_plugin") {
        apply(plugin = "maven-publish")
        apply(plugin = "net.neoforged.moddev")
        apply(plugin = "com.diffplug.spotless")
        apply(plugin = "com.github.spotbugs")

        spotless {

            if (project.name != "endercore") {
                ratchetFrom = "origin/dev/1.21.1"
            }

            encoding("UTF-8")

            java {
                cleanthat().version("2.8")

                eclipse().configFile("$rootDir/config/codeformat/codeformat.xml")

                // Revert to spaces, thank you eclipse
                indentWithSpaces(4)

                importOrder()
                removeUnusedImports()
                trimTrailingWhitespace()
                endWithNewline()
            }
        }

        spotbugs {
            reportsDir = project.layout.buildDirectory.dir("reports/spotbugs/")
            effort = Effort.MAX
            ignoreFailures = true
        }

        tasks.withType<SpotBugsTask> {
            reports {
                create("html") {
                    required = true
                    outputLocation = project.layout.buildDirectory.file("reports/spotbugs/spotbugs.html")
                }
            }
        }

        publishing {
            repositories {
                if (System.getenv("RVR_MAVEN_USER") != null) {
                    maven {
                        name = "Rover656"
                        url = URI("https://maven.rover656.dev/releases")
                        credentials {
                            username = System.getenv("RVR_MAVEN_USER")
                            password = System.getenv("RVR_MAVEN_PASSWORD")
                        }
                    }
                }
            }
        }
    }
}

// ============
// Utilities
// ============

fun getReleaseType(): String? {
    // If we"re doing a proper build
    if (System.getenv("BUILD_VERSION") != null) {
        val version_string = System.getenv("BUILD_VERSION")

        if (version_string.lowercase().contains("alpha")) {
            return "alpha"
        } else if (version_string.lowercase().contains("beta")) {
            return "beta"
        }

        return "release"
    }

    return "dev"
}
