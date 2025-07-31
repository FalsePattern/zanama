plugins {
    id("com.gradle.plugin-publish") version "1.3.1"
    `kotlin-dsl`
    signing
}

group = "com.falsepattern"

version = "0.1.0-SNAPSHOT"

kotlin {
    jvmToolchain(21)
}

fun CopySpec.addZig() {
    from("zig") {
        into("zig")
        include("build.zig", "build.zig.zon", "src/**")
    }
}

tasks.jar {
    archiveBaseName = "zanama"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.VERSION_21.majorVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
    withSourcesJar()
    withJavadocJar()
}

repositories {
    mavenCentral()
}

dependencies {
    implementation("org.ainslec:picocog:1.0.7")
    implementation("com.google.code.gson:gson:2.11.0")
}

val extraFiles = listOf("../LICENSE", "../LICENSES/GPL-3.0-only.txt", "../README.MD")

tasks.named<ProcessResources>("processResources") {
    from(extraFiles)
    addZig()
}

tasks.named<Jar>("sourcesJar") {
    from(extraFiles)
    addZig()
}

gradlePlugin {
    website.set("https://gtmega.falsepattern.com/falsepattern/zanama")
    vcsUrl.set("https://gtmega.falsepattern.com/falsepattern/zanama")
    plugins {
        create("zanama") {
            id = "com.falsepattern.zanama"
            implementationClass = "com.falsepattern.zanama.ZanamaPlugin"
            displayName = "Zanama"
            description = "A Zig to Java FFI bindings generator, inspired by JExtract."
            tags.set(listOf("zig", "ffi", "panama"))
        }
    }
}

// For staging builds
publishing {
    repositories {
        maven {
            name = "mavenpattern"
            setUrl("https://mvn.falsepattern.com/releases/")
        }
    }
}

signing {
    useGpgCmd()
    afterEvaluate {
        sign(publishing.publications["pluginMaven"], publishing.publications["zanamaPluginMarkerMaven"])
    }
}

// Reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}