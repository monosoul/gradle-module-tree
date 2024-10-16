# Gradle module tree plugin

[![Build Status](https://github.com/monosoul/gradle-module-tree/actions/workflows/build-on-push-to-main.yml/badge.svg?branch=main)](https://github.com/monosoul/gradle-module-tree/actions/workflows/build-on-push-to-main.yml?query=branch%3Amain)
[![codecov](https://codecov.io/gh/monosoul/gradle-module-tree/graph/badge.svg?token=AQu3Ntq1z3)](https://codecov.io/gh/monosoul/gradle-module-tree)
[![Gradle Plugins Release](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fmonosoul%2Fmodule-tree%2Fdev.monosoul.module-tree.gradle.plugin%2Fmaven-metadata.xml&label=Gradle%20Plugin%20Portal)](https://plugins.gradle.org/plugin/dev.monosoul.module-tree)
[![Maven metadata URL](https://img.shields.io/maven-metadata/v?metadataUrl=https%3A%2F%2Frepo1.maven.org%2Fmaven2%2Fdev%2Fmonosoul%2Fmodule-tree%2Fdev.monosoul.module-tree.gradle.plugin%2Fmaven-metadata.xml&label=Maven%20Central)](https://mvnrepository.com/artifact/dev.monosoul.gradle.module.tree/gradle-module-tree)
[![license](https://img.shields.io/github/license/monosoul/gradle-module-tree.svg)](LICENSE)
[![Semantic Release](https://img.shields.io/badge/%20%20%F0%9F%93%A6%F0%9F%9A%80-semantic--release-e10079.svg)](https://github.com/semantic-release/semantic-release)


This plugin provides an extensions function to declare Gradle modules as a tree.

Imagine having the following module structure:

```kotlin
include("top-level-module")

include("top-level-module:second-level-module")
include("top-level-module:third-level-module")
project("top-level-module:third-level-module").projectDir = file("top-level-module/second-level-dir/third-level-module")
```

This declaration is hard to read and build a mental model for, especially if you want to have some modules to be in 
subdirectories rather than submodules (to avoid creating unnecessary empty modules).

Here's what the same declaration will look like using the function provided by the plugin:

```kotlin
import dev.monosoul.gradle.module.tree.includeTree

includeTree {
    module("top-level-module") {
        module("second-level-module")
        dir("second-level-dir") {
            module("third-level-module")
        }
    }
}
```

This declaration is much easier to read, and it offers a nice visual representation of the modules structure.

It will also automatically create directory structure and empty `build.gradle.kts` files for every module.


## Getting Started

To apply the plugin simply add it to the plugins block of your `settings.gradle.kts`:

```kotlin
plugins {
    id("dev.monosoul.module-tree") version "0.0.2"
}
```
