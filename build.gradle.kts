import org.jetbrains.intellij.platform.gradle.tasks.GenerateLexerTask
import org.jetbrains.intellij.platform.gradle.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("org.jetbrains.intellij.platform.grammarkit") version "2.18.1"
}

group = "eu.bcosp"
version = providers.environmentVariable("PLUGIN_VERSION").getOrElse("1.0.0-SNAPSHOT")

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

// Read more: https://plugins.jetbrains.com/docs/intellij/tools-intellij-platform-gradle-plugin.html
dependencies {
    intellijPlatform {
        intellijIdea("2025.2.6.2")
        bundledPlugin("org.toml.lang")
        bundledPlugin("org.jetbrains.plugins.yaml")
        testFramework(org.jetbrains.intellij.platform.gradle.TestFrameworkType.Platform)
    }
    testImplementation("junit:junit:4.13.2")
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "252.25557"
        }

        changeNotes = providers.environmentVariable("CHANGE_NOTES").getOrElse(
            """
                Initial version
            """.trimIndent()
        )
    }

    signing {
        certificateChain.set(providers.environmentVariable("CERTIFICATE_CHAIN"))
        privateKey.set(providers.environmentVariable("PRIVATE_KEY"))
        password.set(providers.environmentVariable("PRIVATE_KEY_PASSWORD"))
    }

    publishing {
        token.set(providers.environmentVariable("PUBLISH_TOKEN"))
        // Published to the "default" (stable) channel unless the version has a "-<name>" suffix
        // (e.g. "1.2.0-beta"), in which case it ships to that channel instead - this lets a
        // pre-release tag go out as a beta build without any extra configuration.
        channels.set(listOf(version.toString().substringAfter('-', "default")))
    }

    pluginVerification {
        ides {
            recommended()
        }
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

    targetRootOutputDir.set(genOutputDir)
    pathToClass.set("eu/bcosp/vrlintellij/grammars/VRLLexer.java")

    purgeOldFiles.set(true)
}

val generateSyntaxParser = tasks.register<GenerateParserTask>("generateSyntaxParser") {
    sourceFile.set(file("src/main/kotlin/eu/bcosp/vrlintellij/grammars/VRL.bnf"))

    targetRootOutputDir.set(genOutputDir)
    pathToParser.set("eu/bcosp/vrlintellij/parser/VRLParser.java")
    pathToPsiRoot.set("eu/bcosp/vrlintellij/psi")

    purgeOldFiles.set(true)
}

val generateStringTemplateLexer = tasks.register<GenerateLexerTask>("generateStringTemplateLexer") {
    sourceFile.set(file("src/main/kotlin/eu/bcosp/vrlintellij/highlighting/template/VRLStringTemplateLexer.flex"))

    targetRootOutputDir.set(genOutputDir)
    pathToClass.set("eu/bcosp/vrlintellij/highlighting/template/VRLStringTemplateLexer.java")

    purgeOldFiles.set(true)
}

val generate by tasks.registering {
    outputs.dir(genOutputDir)
    dependsOn(generateSyntaxLexer, generateSyntaxParser, generateStringTemplateLexer)
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
