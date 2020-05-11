import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
plugins {
    id("java")
    kotlin("jvm") version "1.3.72"
    kotlin("plugin.serialization") version "1.3.72"
    id("com.github.johnrengelman.shadow") version "5.2.0"
}

group = "pl.edu.moe"
version = "1.1.1"

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
    implementation("net.mamoe:mirai-core-qqandroid:1.0-RC2-1")
    implementation("com.google.guava:guava:29.0-jre")
    implementation("net.mamoe.yamlkt:yamlkt:0.2.0")
    testImplementation("junit:junit:4.12")
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