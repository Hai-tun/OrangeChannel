import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("java")
    kotlin("jvm") version "1.4.10"
    kotlin("plugin.serialization") version "1.4.10"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

group = "pl.edu.moe"
version = "1.1.2"

repositories {
    maven { url = uri("https://maven.aliyun.com/repository/public") }
    maven { url = uri("https://maven.aliyun.com/repository/jcenter") }
    maven { url = uri("https://maven.aliyun.com/repository/google") }
    maven { url = uri("https://mirrors.huaweicloud.com/respository/maven") }
    maven { url = uri("https://dl.bintray.com/kotlin/kotlin-eap") }
    mavenCentral()
    jcenter()
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("me.xuxiaoxiao:chatapi-wechat:1.4.0")
    implementation("net.mamoe:mirai-core-qqandroid:1.3.1")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("net.mamoe.yamlkt:yamlkt:0.6.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json-jvm:1.0.0-RC2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.7.0")
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
    kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
}

tasks.withType<Jar> {
    manifest {
        attributes(
            mapOf(
                "Main-Class" to "pl.edu.moe.orangechannel.MainKt"
            )
        )
    }
}