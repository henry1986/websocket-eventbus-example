plugins {
    kotlin("multiplatform") version "1.6.10"
    kotlin("plugin.serialization") version "1.6.10"
}

group = "org.daiv.example"
version = "1.0"

repositories {
    mavenCentral()
    maven("https://artifactory.daiv.org/artifactory/gradle-dev-local")
}

kotlin {
    /* Targets configuration omitted. 
    *  To find out how to configure the targets, please follow the link:
    *  https://kotlinlang.org/docs/reference/building-mpp-with-gradle.html#setting-up-targets */
    jvm {
        compilations.all {
            kotlinOptions.jvmTarget = "1.8"
        }
        testRuns["test"].executionTask.configure {
            useJUnit()
        }
    }
    js(LEGACY) {
        browser {
            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.daiv.util:kutil:0.5.1")
                implementation("org.daiv.websocket:eventbus:0.7.2")
                implementation("org.daiv.coroutines:coroutines-lib:0.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-core:1.4.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val versionsktor = "2.1.3"
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-server-websockets:${versionsktor}")
                implementation("io.ktor:ktor-server-netty:${versionsktor}")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
//        val nativeMain by getting
//        val nativeTest by getting
    }
    val assembleWeb = tasks.register("assembleWeb"){
        doLast {
            val name = "websocket-eventbus-example"
            val distributions = "distributions"
            val jsFolder = "js"
            val dist = "${project.buildDir}/$distributions"
            val fjs = File("$dist/$name.js")
            val fjsMap = File("${project.buildDir}/$distributions/$name.js.map")
            File("${project.buildDir}/$distributions/$jsFolder/").mkdirs()
            val rename = "${project.buildDir}/$distributions/$jsFolder/$name.js"
            fjs.renameTo(File(rename))
            fjsMap.renameTo(File("${project.buildDir}/$distributions/$jsFolder/$name.js.map"))
        }
//    from("${project.buildDir}/distributions/frontend.js")
//    from("${project.buildDir}/distributions/frontend.js")
//    into("${project.buildDir}/distributions/js/")
    }

//tasks.getByName("jsBrowserDevelopmentWebpack"){
//    dependsOn(assembleWeb)
//}
    tasks.getByName("assembleWeb"){
        dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
    }

    tasks.getByName<Jar>("jvmJar"){
        dependsOn(assembleWeb)
        dependsOn(tasks.getByName("jsBrowserProductionWebpack"))
        val jsBrowserProductionWebpack = tasks.getByName<org.jetbrains.kotlin.gradle.targets.js.webpack.KotlinWebpack>("jsBrowserProductionWebpack")
        from(File(jsBrowserProductionWebpack.destinationDirectory, jsBrowserProductionWebpack.outputFileName))
        from(File(jsBrowserProductionWebpack.destinationDirectory, "${jsBrowserProductionWebpack.outputFileName}.map"))
    }

}