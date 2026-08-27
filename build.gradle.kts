import org.jetbrains.grammarkit.tasks.GenerateLexerTask
import org.jetbrains.grammarkit.tasks.GenerateParserTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.4.10"
    id("org.jetbrains.intellij.platform") version "2.18.1"
    id("org.jetbrains.grammarkit") version "2023.3.0.4"
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
        intellijIdea("2025.2.4")
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

val generateStringTemplateLexer = tasks.register<GenerateLexerTask>("generateStringTemplateLexer") {
    sourceFile.set(file("src/main/kotlin/eu/bcosp/vrlintellij/highlighting/template/VRLStringTemplateLexer.flex"))

    // Must NOT be nested under generateSyntaxLexer's targetOutputDir (.../grammars): its own
    // purgeOldFiles recursively purges that whole subtree, including subdirectories owned by
    // other tasks, whenever it runs a fresh (non-cached) generation - it doesn't just remove
    // stale output that used to be its own.
    targetOutputDir.set(file("$genOutputDir/eu/bcosp/vrlintellij/highlighting/template"))

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
