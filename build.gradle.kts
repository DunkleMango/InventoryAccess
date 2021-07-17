import org.spongepowered.gradle.plugin.config.PluginLoaders
import org.spongepowered.plugin.metadata.PluginDependency

plugins {
    `java-library`
    id("org.spongepowered.gradle.plugin") version "1.1.1"
}

group =  "io.github.dunklemango"
version = "1.0.0"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.7.0")
    testImplementation("org.mockito:mockito-inline:3.+")
    api("org.slf4j:slf4j-api:1.7.31")
    implementation("org.slf4j:slf4j-simple:1.7.31")
    testImplementation("org.slf4j:slf4j-simple:1.7.31")
    api("org.spongepowered:spongeapi:7.3.0")
    annotationProcessor("org.spongepowered:spongeapi:7.3.0")
}

tasks.test {
    useJUnitPlatform()
}

sponge {
    apiVersion("7.3.0")
    plugin("inventoryaccess") {
        loader(PluginLoaders.JAVA_PLAIN)
        displayName("Inventory Access")
        mainClass("io.github.dunklemango.InventoryAccessPlugin")
        description("Enables the access of other players inventories.")
        links {
            source("https://github.com/DunkleMango/InventoryAccess")
            issues("https://github.com/DunkleMango/InventoryAccess/issues")
        }
        contributor("DunkleMango") {
            description("Lead Developer")
        }
        dependency("spongeapi") {
            loadOrder(PluginDependency.LoadOrder.AFTER)
            optional(false)
        }
    }
}

val javaTarget = 8 // Sponge targets a minimum of Java 8
java {
    sourceCompatibility = JavaVersion.toVersion(javaTarget)
    targetCompatibility = JavaVersion.toVersion(javaTarget)
}

tasks.withType(JavaCompile::class).configureEach {
    options.apply {
        encoding = "utf-8" // Consistent source file encoding
        if (JavaVersion.current().isJava10Compatible) {
            release.set(javaTarget)
        }
    }
}

// Make sure all tasks which produce archives (jar, sources jar, javadoc jar, etc) produce more consistent output
tasks.withType(AbstractArchiveTask::class).configureEach {
    isReproducibleFileOrder = true
    isPreserveFileTimestamps = false
}