import org.gradle.api.tasks.testing.logging.TestExceptionFormat.FULL
import org.gradle.api.tasks.testing.logging.TestLogEvent.FAILED
import org.gradle.api.tasks.testing.logging.TestLogEvent.PASSED
import org.gradle.api.tasks.testing.logging.TestLogEvent.STARTED
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    `kotlin-dsl`
    id("com.gradle.plugin-publish") version "1.3.0"
    id("io.github.gradle-nexus.publish-plugin") version "2.0.0"
    id("pl.droidsonroids.jacoco.testkit") version "1.0.12"
    jacoco
    signing
}

group = "dev.monosoul.gradle.module.tree"

val targetJava = JavaVersion.VERSION_1_8
java {
    sourceCompatibility = targetJava
    targetCompatibility = targetJava
}
kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.fromTarget("$targetJava")
    }
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

val siteUrl = "https://github.com/monosoul/gradle-module-tree"
val githubUrl = "https://github.com/monosoul/gradle-module-tree"

val pluginName = "Module tree plugin"
val pluginDescription = "Provides functions to declare modules as a tree"

signing {
    val signingKeyId: String? by project
    val signingKey: String? by project
    val signingPassword: String? by project
    val withSigning: String? by project
    useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword)
    setRequired({
        withSigning.toBoolean() && gradle.taskGraph.hasTask("publish")
    })
}

nexusPublishing {
    repositories {
        sonatype {
            nexusUrl.set(uri("https://s01.oss.sonatype.org/service/local/"))
            snapshotRepositoryUrl.set(uri("https://s01.oss.sonatype.org/content/repositories/snapshots/"))
        }
    }
}

gradlePlugin {
    website.set(siteUrl)
    vcsUrl.set(githubUrl)

    plugins.create("moduleTreePlugin") {
        id = "dev.monosoul.module-tree"
        implementationClass = "dev.monosoul.gradle.module.tree.ModuleTreePlugin"
        version = project.version

        displayName = pluginName
        description = pluginDescription

        tags.set(listOf("module", "tree", "settings", "submodule", "project"))
    }
}

publishing {
    publications {
        withType<MavenPublication> {
            pom {
                name.set(pluginName)
                description.set(pluginDescription)
                url.set(siteUrl)
                scm {
                    url.set(githubUrl)
                    connection.set("scm:git:$githubUrl.git")
                    developerConnection.set("scm:git:$githubUrl.git")
                }
                developers {
                    developer {
                        id.set("monosoul")
                        name.set("Andrei Nevedomskii")
                    }
                }
                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }
                issueManagement {
                    url.set("$githubUrl/issues")
                }
            }
        }
    }

    repositories {
        maven {
            name = "Snapshot"
            url = uri("https://maven.pkg.github.com/monosoul/gradle-module-tree")
            credentials {
                username = System.getenv("GITHUB_ACTOR")
                password = System.getenv("GITHUB_TOKEN")
            }
        }
        val localRepositoryDirName by project.extra { "local-repository" }
        maven {
            name = "localBuild"
            url = uri("build/$localRepositoryDirName")
        }
    }
}

dependencies {
    testImplementation(enforcedPlatform("org.junit:junit-bom:5.11.2"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

    testImplementation(gradleTestKit())
    testImplementation("io.strikt:strikt-jvm:0.34.1")
}

tasks {
    named<Test>("test") {
        useJUnitPlatform()

        maxHeapSize = "1G"

        testLogging {
            events(STARTED, PASSED, FAILED)
            showExceptions = true
            showStackTraces = true
            showCauses = true
            exceptionFormat = FULL
        }
    }

    val testTasks = withType<Test>()
    jacocoTestReport {
        executionData.setFrom(
            testTasks.map { it.extensions.getByType<JacocoTaskExtension>().destinationFile }
        )

        reports {
            xml.required.set(true)
            html.required.set(false)
        }
        shouldRunAfter(testTasks)
    }
}
