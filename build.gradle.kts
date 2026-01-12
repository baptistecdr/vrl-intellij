import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.20"
    id("org.jetbrains.intellij.platform") version "2.10.2"
    id("org.jetbrains.grammarkit") version "2023.3.0.1"
}

group = "eu.bcosp"
version = "1.0.0-SNAPSHOT"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea("2025.2.4")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252.25557"
        }

        changeNotes = """
            Initial version
        """.trimIndent()
    }
}

tasks {
    // Set the JVM compatibility versions
    withType<JavaCompile> {
        sourceCompatibility = "21"
        targetCompatibility = "21"
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_21)
    }
}

val genOutputDir = file("src/main/gen")

val generateSyntaxLexer = tasks.register<GenerateLexerTask>("generateSyntaxLexer") {
    sourceFile.set(file("src/main/kotlin/eu/bcosp/vrlintellij/grammars/VRLLexer.flex"))

    targetOutputDir.set(file("$genOutputDir/eu/bcosp/vrlintellij/grammars"))

    purgeOldFiles.set(true)
}

val generateSyntaxParser = tasks.register<GenerateParserTask>("generateSyntaxParser") {
    sourceFile.set(file("src/main/kotlin/eu/bcosp/vrlintellij/grammars/VRL.bnf"))

    targetRootOutputDir.set(genOutputDir)
    pathToParser.set("$genOutputDir/eu/bcosp/vrlintellij/parser/VRLParser.java")
    pathToPsiRoot.set("$genOutputDir/eu/bcosp/vrlintellij/psi")

    purgeOldFiles.set(true)
}

val generate by tasks.registering {
    outputs.dir(genOutputDir)
    dependsOn(generateSyntaxLexer, generateSyntaxParser)
}

sourceSets {
    main {
        java.srcDirs("src/main/kotlin", generate)
    }
    test {
        java.srcDirs("src/test/kotlin")
    }
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(generate)
}

tasks.withType<KotlinCompile>().configureEach {
    dependsOn(generate)
}

tasks.clean {
    delete(generate)
}

idea {
    module {
        generatedSourceDirs = setOf(genOutputDir)
    }
}
