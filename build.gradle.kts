import groovy.json.JsonOutput
import groovy.json.JsonSlurper
import org.apache.tools.ant.filters.ReplaceTokens
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter

plugins {
    id("eclipse")
    id("idea")
    id("net.neoforged.gradle.userdev") version "7.+"
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(17)
    }
}

val mod_version: String by project.properties
val minecraft_version: String by project.properties
val neo_version: String by project.properties
val mod_id: String by project.properties

version = mod_version
val archivesBaseName = "ICBM-classic-${minecraft_version}"

base {
    archivesName = mod_id
}

//fancyGradle {
//    patches {
//        //https://gitlab.com/gofancy/fancygradle/-/wikis/IDE-Specific-Runs
//        resources
//        coremods
//        asm
//        mergetool
//    }
//}

idea {
    module {
//        outputDir = compileJava.destinationDir
//        testOutputDir = compileTestJava.destinationDir
        inheritOutputDirs = true
        isDownloadJavadoc = true
        isDownloadSources = true
    }
}

dependencies {
    implementation("net.neoforged:neoforge:$neo_version")

    // https://projectlombok.org/setup/gradle
    compileOnly("org.projectlombok:lombok:1.18.24")
    annotationProcessor("org.projectlombok:lombok:1.18.24")

    testCompileOnly("org.projectlombok:lombok:1.18.24")
    testAnnotationProcessor("org.projectlombok:lombok:1.18.24")

    //Mods
    implementation(files("libs/NBT-Exporter-1.0-forgelib.jar"))

    //Junit 5
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testImplementation("org.junit.jupiter:junit-jupiter-params:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
    testImplementation(files("libs/MinecraftJUnit-1.5.0-deobf.jar")) //TODO use forge gradle to deobf for me testImplementation fg.deobf('whateveridc:MinecraftJUnit:1.4.0')
    //TODO example  runtimeOnly(fg.deobf(group = "mezz.jei", name = "jei-1.16.5", version = "7.7.1.121"))

    //Resource injector
    // https://hosuaby.github.io/inject-resources/0.3.2/asciidoc/#inject-resources-junit-jupiter
    // https://github.com/hosuaby/inject-resources
    testImplementation("io.hosuaby:inject-resources-junit-jupiter:0.3.2")

    //Mockito
    testImplementation("org.mockito:mockito-inline:4.3.1")
    testImplementation("org.mockito:mockito-core:4.3.1")
}


minecraft {
    mappings {

    }
    accessTransformers {
        file("src/main/resources/META-INF/ICBMClassic_at.cfg")
    }

    runs {
        configureEach {
            systemProperty("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
            systemProperty("forge.logging.console.level", "debug")
        }
        register("client") {
            workingDirectory(project.file("run/client"))
        }
        register("server") {
            workingDirectory(project.file("run/server"))
        }
    }
}

val filterTokens = tasks.register<Sync>("filterTokens") {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // this will ensure that this task is redone when the versions change.
    inputs.property("version", version)

    from(sourceSets.main.get().java)
    filter(ReplaceTokens::class, mapOf("VERSION" to version.toString()))
    into(layout.buildDirectory.dir("sources"))
}

//tasks.compileJava {
//    source(filterTokens.map { it.outputs.files })
//}

tasks.test {
    useJUnitPlatform()

    val dir = layout.projectDirectory.dir("run/tests")
    maxHeapSize = "1G"
    failFast = false
    workingDir(dir)
    mkdir(dir)

    forkEvery = 1
}

tasks.processResources {

    // required to allow file expansion later
    duplicatesStrategy = DuplicatesStrategy.INCLUDE

    // this will ensure that this task is redone when the versions change.
    inputs.property("version", version)
    inputs.property("mcversion", minecraft_version)

    // replace stuff in mcmod.info, nothing else
    from(sourceSets.main.map { it.resources.sourceDirectories }) {
        include("mcmod.info")

        // replace version and mcversion
        expand("version" to version, "mcversion" to minecraft_version)
    }

    // copy everything else except the mcmod.info
    from(sourceSets.main.map { it.resources.sourceDirectories }) {
        exclude("mcmod.info")
    }

    //Borrow from darkhax, thank you
    doLast {

        val jsonMinifyStart = System.currentTimeMillis()
        var jsonMinified = 0
        var jsonBytesSaved: Long = 0

        fileTree("dir" to outputs.files.asPath, "include" to "**/*.json").forEach { file ->
            jsonMinified++
            val oldLength = file.length()
            file.writeText(JsonOutput.toJson(JsonSlurper().parse(file)))
            jsonBytesSaved += oldLength - file.length()
        }

        println("Minified $jsonMinified json files. Saved $jsonBytesSaved bytes. Took ${System.currentTimeMillis() - jsonMinifyStart}ms.")
    }
}

tasks.jar {
    val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssZ")
    manifest.attributes(
            "Built-By" to System.getProperty("user.name"),
            "Created-By" to "Gradle ${gradle.gradleVersion}",
            "Build-Jdk" to "${System.getProperty("java.version")} (${System.getProperty("java.vendor")} ${System.getProperty("java.vm.version")})",
            "Build-OS" to "${System.getProperty("os.name")} ${System.getProperty("os.arch")} ${System.getProperty("os.version")}",
            "Specification-Title" to "ICBM-Classic",
            "Specification-Vendor" to "Built Broken Modding",
            "Specification-Version" to "${project.version}",
            "Implementation-Title" to archivesBaseName,
            "Implementation-Version" to "${project.version}",
            "Implementation-Timestamp" to formatter.format(ZonedDateTime.now()),
            "FMLAT" to "ICBMClassic_at.cfg")
    finalizedBy("reobfJar")
}

repositories {
    mavenCentral()
    maven {
        name = "SquidDev"
        url = uri("https://squiddev.cc/maven/")
    }
}

tasks.register<Copy>("installLocalGitHook"){
    from(layout.projectDirectory.file("scripts/commit-msg"))
    into(layout.projectDirectory.dir(".git/hooks"))
    setFileMode(775)
}

defaultTasks("installLocalGitHook")