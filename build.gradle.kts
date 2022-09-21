@file:Suppress("PropertyName")

val ktor_version: String by project
val kotlin_version: String by project
val logback_version: String by project
val ktx_html_version: String by project

val kotlin_react_version: String by project
val kotlin_react_router_version: String by project

rootProject.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin> {
    rootProject.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension>().nodeVersion = "16.0.0"
}


plugins {
    application
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "ru.diamant.rabbit"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven")
}

kotlin {
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        withJava()
        testRuns["test"].executionTask.configure {
            useJUnitPlatform()
            minHeapSize = "1024m"
            maxHeapSize = "4096m"
        }
    }
    js(LEGACY) {
        binaries.executable()
        browser {
            commonWebpackConfig {
                cssSupport.enabled = true
            }
        }
    }

    @Suppress("UNUSED_VARIABLE")
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }


        val jvmMain by getting {
            dependencies {
                // https://ktor.io/docs/engines.html
                implementation("io.ktor:ktor-server-netty:$ktor_version")
                implementation("at.favre.lib:bcrypt:0.9.0")

                // https://ktor.io/docs/html-dsl.html
                implementation("io.ktor:ktor-html-builder:$ktor_version")
                implementation("org.jetbrains.kotlinx:kotlinx-html-jvm:$ktx_html_version")

                // https://ktor.io/docs/session-auth.html
                implementation("io.ktor:ktor-auth:$ktor_version")
                implementation("io.ktor:ktor-server-sessions:$ktor_version")

                // https://ktor.io/docs/kotlin-serialization.html
                implementation("io.ktor:ktor-serialization:$ktor_version")

                // https://ktor.io/docs/logging.html
                implementation("ch.qos.logback:logback-classic:$logback_version")

                // https://docs.mongodb.com/manual/administration/install-community/
                // https://litote.org/kmongo
                implementation("org.litote.kmongo:kmongo-coroutine:4.2.8")
                implementation("org.jsoup:jsoup:1.14.3")
                implementation("commons-cli:commons-cli:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.2")
                implementation("commons-io:commons-io:2.11.0")
                implementation("io.github.microutils:kotlin-logging-jvm:2.1.21")
                implementation("org.slf4j:slf4j-api:1.7.36")
                implementation("org.slf4j:slf4j-simple:1.7.36")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation("org.junit.jupiter:junit-jupiter:5.8.1")
                // https://ktor.io/docs/testing.html
                implementation("io.ktor:ktor-server-test-host:$ktor_version")
            }
        }


        val jsMain by getting {
            dependencies {
                // https://kotlinlang.org/docs/js-get-started.html
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react:$kotlin_react_version")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-dom:$kotlin_react_version")
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-css:$kotlin_react_version")

                // https://github.com/JetBrains/kotlin-wrappers/tree/master/kotlin-react-router-dom
                implementation("org.jetbrains.kotlin-wrappers:kotlin-react-router-dom:$kotlin_react_router_version")

                // https://ktor.io/docs/getting-started-ktor-client.html
                implementation("io.ktor:ktor-client-js:$ktor_version")

                // https://ktor.io/docs/json.html
                implementation("io.ktor:ktor-client-serialization:$ktor_version")
            }
        }
        val jsTest by getting
    }
}

application {
    mainClass.set("ru.diamant.rabbit.application.ServerKt")
}

tasks.named<Copy>("jvmProcessResources") {
    val jsBrowserDistribution = tasks.named("jsBrowserDistribution")
    from(jsBrowserDistribution)
}

tasks.named<JavaExec>("run") {
    dependsOn(tasks.named<Jar>("jvmJar"))
    classpath(tasks.named<Jar>("jvmJar"))
}