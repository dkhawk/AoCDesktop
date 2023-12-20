import org.jetbrains.compose.compose
import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.10"
    id("org.jetbrains.compose") version "1.1.1"
}

group = "me.dkhawk"
version = "1.0"

repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
}

object Versions {
    //    const val gradle = "3.5.0"
    const val kotlin = "1.6.0"
//    const val appcompat = "1.0.2"

    /* test */
//    const val junit = "4.12"
}

dependencies {
    implementation("com.google.truth:truth-parent:1.1.3")
    testImplementation(kotlin("test"))
    implementation(compose.desktop.currentOs)
    implementation("org.jetbrains.compose.material:material-icons-extended-desktop:1.1.1")
    implementation("com.google.truth:truth:1.1.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.2")
    implementation("org.jetbrains.kotlin:kotlin-reflect:${Versions.kotlin}")
    implementation("org.jetbrains.kotlin:kotlin-test:${Versions.kotlin}")
}

tasks.test {
    useJUnit()
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

compose.desktop {
    application {
        mainClass = "MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "aocDesktop"
            packageVersion = "1.0.0"
        }
    }
}
