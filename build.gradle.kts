import com.falsepattern.zanama.tasks.ZanamaTranslate
import com.falsepattern.zigbuild.tasks.ZigBuildTask
import com.falsepattern.zigbuild.toolchain.ZigVersion

plugins {
    java
    `java-library`
    signing
    id("com.falsepattern.zigbuild") version "0.1.1"
    id("com.falsepattern.zanama")
    id("com.vanniktech.maven.publish") version "0.34.0"
}

group = "com.falsepattern"

version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(JavaVersion.VERSION_24.majorVersion)
        vendor = JvmVendorSpec.ADOPTIUM
    }
}

val extraFiles = listOf("src/main/LICENSE", "LICENSES")

tasks.processResources {
    from(extraFiles)
}

afterEvaluate {
    tasks.named<Jar>("sourcesJar") {
        from(extraFiles)
    }
}

tasks.javadoc {
    exclude("com/ventooth/vnativeloader/internal")
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.38")
    annotationProcessor("org.projectlombok:lombok:1.18.38")
    compileOnly("org.jetbrains:annotations:26.0.2")

    implementation("commons-io:commons-io:2.16.1")
    implementation("org.apache.logging.log4j:log4j-api:2.0")
    implementation("commons-codec:commons-codec:1.17.0")

    testImplementation("org.apache.logging.log4j:log4j-core:2.25.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

zig {
    toolchain {
        version = ZigVersion.of("0.14.1")
    }
}

tasks {
    compileJava {
        options.forkOptions.jvmArgs = (options.forkOptions.jvmArgs ?: emptyList()) + "--sun-misc-unsafe-memory-access=allow"
    }
    val zigSources = arrayOf(
        layout.projectDirectory.file("build.zig"),
        layout.projectDirectory.file("build.zig.zon"),
        layout.projectDirectory.dir("src/test/zig"),
        layout.projectDirectory.file("plugin/zig/build.zig"),
        layout.projectDirectory.file("plugin/zig/build.zig.zon"),
        layout.projectDirectory.dir("plugin/zig/src"),
    )
    val zigTest = register<ZigBuildTask>("zigTest") {
        options {
            steps.add("selfTest")
        }
        prefixDirectory = layout.buildDirectory.dir("zig-test")
        clearPrefixDirectory = true
        sourceFiles.from(*zigSources)
    }

    val zigHostDir = layout.buildDirectory.dir("zig-host")
    val zigInstallHost = register<ZigBuildTask>("zigInstallHost") {
        options {
            steps.add("installHost")
        }
        prefixDirectory = zigHostDir
        clearPrefixDirectory = true
        sourceFiles.from(*zigSources)
    }

    val translateJavaSources = layout.buildDirectory.dir("generated/zanama_root")

    val zigTranslateHost = register<ZanamaTranslate>("zigTranslateHost") {
        from = zigHostDir.map { it.file("root.json") }
        into = translateJavaSources
        rootPkg = "com.falsepattern.zanama.testing.natives"
        bindRoot = "com.falsepattern.zanama.testing"
        className = "root_z"
        dependsOn(zigInstallHost)
    }

    sourceSets["test"].java.srcDir(translateJavaSources)

    compileTestJava {
        dependsOn(zigTranslateHost)
    }

    test {
        useJUnitPlatform()
        dependsOn(zigTest)
        @Suppress("USELESS_ELVIS") //IDE is lying
        jvmArgs = (jvmArgs ?: emptyList()) + "--enable-native-access=ALL-UNNAMED"
    }
}

mavenPublishing {
    publishToMavenCentral()

    signAllPublications()

    pom {
        name = "Zanama"
        description = "A Zig to Java FFI bindings generator, inspired by JExtract."
        url = "https://gtmega.falsepattern.com/falsepattern/zanama"
        licenses {
            license {
                name = "GNU Lesser General Public License v3.0 only"
                url = "https://www.gnu.org/licenses/lgpl-3.0.en.html"
            }
        }
        developers {
            developer {
                name = "FalsePattern"
                url = "https://falsepattern.com"
                email = "me@falsepattern.com"
            }
        }
        scm {
            connection = "scm:git:git://gtmega.falsepattern.com/FalsePattern/zanama.git"
            developerConnection = "scm:git:ssh://gtmega.falsepattern.com/falsepattern/zanama.git"
            url = "https://gtmega.falsepattern.com/falsepattern/zanama/"
        }
    }
}

publishing {
    repositories {
        maven {
            name = "mavenpattern"
            setUrl("https://mvn.falsepattern.com/releases/")
            credentials(PasswordCredentials::class.java)
        }
    }
}

signing {
    useGpgCmd()
}

// Reproducible builds
tasks.withType<AbstractArchiveTask>().configureEach {
    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true
}