package dev.monosoul.gradle.module.tree

import org.gradle.api.initialization.Settings
import java.io.File

data class IncludeTree(private val settings: Settings, private val path: String, private val parentProject: String) {
    fun dir(path: String, block: IncludeTree.() -> Unit) {
        val nestedPath = "${this.path}/$path"
        settings.includeTree(nestedPath, parentProject, block)
    }

    fun module(name: String, block: IncludeTree.() -> Unit = {}) {
        val projectName = "$parentProject:$name"
        val projectDir = "$path/$name"

        settings.include(projectName)
        settings.project(projectName).also {
            it.projectDir = File(settings.rootDir, projectDir)

            it.buildFile.takeUnless(File::exists)?.also { buildFile ->
                buildFile.parentFile.mkdirs()
                File("${buildFile.absolutePath}.kts").createNewFile()
            }
        }
        settings.includeTree(projectDir, projectName, block)
    }
}

fun Settings.includeTree(path: String = ".", parentProject: String = "", block: IncludeTree.() -> Unit) {
    IncludeTree(this, path, parentProject).block()
}