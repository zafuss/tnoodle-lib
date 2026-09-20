import configurations.Languages.attachRemoteRepositories
import configurations.Languages.configureJava

description = "Native Image bridge used by the RubikBMT mobile app."

plugins {
    `java-library`
}

attachRemoteRepositories()
configureJava()

dependencies {
    implementation(project(":scrambles"))

    compileOnly(libs.graal.sdk)
}

tasks.register<Exec>("nativeImageShared") {
    dependsOn(tasks.classes)

    val outputDirectory = layout.buildDirectory.dir("native-image")
    val runtimeClasspath = configurations.runtimeClasspath
    val mainOutput = sourceSets.main.map { it.output }
    val nativeImage = providers.environmentVariable("NATIVE_IMAGE").orElse("native-image")

    inputs.files(runtimeClasspath)
    inputs.files(mainOutput)
    outputs.file(outputDirectory.map { it.file("lib-scrambles.dylib") })

    doFirst {
        outputDirectory.get().asFile.mkdirs()
    }

    commandLine(
        nativeImage.get(),
        "--shared",
        "--no-fallback",
        "-H:Name=lib-scrambles",
        "-H:Path=${outputDirectory.get().asFile.absolutePath}",
        "-cp",
        files(mainOutput, runtimeClasspath).asPath,
        "vn.rubikbmt.tnoodle.nativebridge.RubikbmtTnoodleNative"
    )
}
