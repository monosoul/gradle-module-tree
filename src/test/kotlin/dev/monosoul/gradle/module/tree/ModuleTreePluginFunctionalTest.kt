package dev.monosoul.gradle.module.tree

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import strikt.api.expect
import strikt.assertions.contains
import strikt.java.exists
import strikt.java.notExists
import java.io.File
import java.io.FileOutputStream

class ModuleTreePluginFunctionalTest {

    @TempDir
    private lateinit var projectDir: File

    @BeforeEach
    fun setUp() {
        copyResource("/testkit-gradle.properties", "gradle.properties")
    }

    @Test
    fun `should create directory structure and empty buildscripts with dot root`() {
        writeProjectFile("build.gradle.kts") { "" }
        writeProjectFile("settings.gradle.kts") {
            """ 
                import dev.monosoul.gradle.module.tree.includeTree
                
                plugins {
                    id("dev.monosoul.module-tree")
                }
                
                includeTree {
                    module("top-level-module") {
                        dir("second-level-dir") {
                            module("third-level-module")
                        }
                    }
                    dir("top-level-dir") {
                        module("second-level-module")
                    }
                }
            """.trimIndent()
        }
        val result = runGradleWithArguments("projects")

        expect {
            that(result.output) contains """
                +--- Project ':second-level-module'
                \--- Project ':top-level-module'
                     \--- Project ':top-level-module:third-level-module'
            """.trimIndent()

            that(
                projectFile("top-level-module/build.gradle.kts")
            ).exists()

            that(
                projectFile("top-level-module/second-level-dir")
            ).exists()
            that(
                projectFile("top-level-module/second-level-dir/build.gradle.kts")
            ).notExists()

            that(
                projectFile("top-level-module/second-level-dir/third-level-module/build.gradle.kts")
            ).exists()

            that(
                projectFile("top-level-dir")
            ).exists()
            that(
                projectFile("top-level-dir/build.gradle.kts")
            ).notExists()

            that(
                projectFile("top-level-dir/second-level-module/build.gradle.kts")
            ).exists()
        }
    }

    @Test
    fun `should create directory structure and empty buildscripts with custom root`() {
        writeProjectFile("build.gradle.kts") { "" }
        writeProjectFile("settings.gradle.kts") {
            """ 
                import dev.monosoul.gradle.module.tree.includeTree
                
                plugins {
                    id("dev.monosoul.module-tree")
                }
                
                includeTree("root") {
                    dir("top-level-dir") {
                        module("second-level-module")
                    }
                }
            """.trimIndent()
        }
        val result = runGradleWithArguments("projects")

        expect {
            that(result.output) contains """
                \--- Project ':second-level-module'
            """.trimIndent()

            that(
                projectFile("root")
            ).exists()
            that(
                projectFile("root/build.gradle.kts")
            ).notExists()

            that(
                projectFile("root/top-level-dir")
            ).exists()
            that(
                projectFile("root/top-level-dir/build.gradle.kts")
            ).notExists()

            that(
                projectFile("root/top-level-dir/second-level-module/build.gradle.kts")
            ).exists()
        }
    }

    private fun writeProjectFile(
        fileName: String,
        bodySupplier: () -> String,
    ) = projectDir.writeChild(fileName, bodySupplier)

    private fun projectFile(fileName: String) = projectDir.getChild(fileName)

    private fun runGradleWithArguments(
        vararg arguments: String,
        projectDirectory: File = projectDir,
    ): BuildResult =
        GradleRunner
            .create()
            .withProjectDir(projectDirectory)
            .withPluginClasspath()
            .forwardOutput()
            .withArguments(*arguments, "--stacktrace", "--info")
            .build()

    private fun File.getChild(fileName: String) = File(this, fileName).also { it.parentFile.mkdirs() }

    private fun File.writeChild(
        fileName: String,
        bodySupplier: () -> String,
    ) = getChild(fileName)
        .writeText(bodySupplier())

    private fun copyResource(
        from: String,
        to: String,
    ) = projectDir.copy(from, to)

    private fun File.copy(
        from: String,
        to: String,
    ) {
        val destinationFile = getChild(to)
        ModuleTreePluginFunctionalTest::class.java.getResourceAsStream(from)?.use { sourceStream ->
            FileOutputStream(destinationFile).use { destinationStream ->
                sourceStream.copyTo(destinationStream)
            }
        } ?: throw IllegalStateException("Resource not found: $from")
    }
}