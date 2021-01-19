package de.jensklingenberg.gradle

import org.gradle.api.Project
import org.gradle.api.provider.Provider
import org.gradle.api.tasks.SourceSetContainer
import org.jetbrains.kotlin.gradle.dsl.*
import org.jetbrains.kotlin.gradle.plugin.*
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinJvmAndroidCompilation
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinMultiplatformPlugin
import org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeCompilation
import java.io.File

open class CabretGradleExtension {
    var enabled: Boolean = true
    var version: String = "1.0.2"
}




val Project.multiplatformExtension: KotlinMultiplatformExtension? get() = project.extensions.findByType(KotlinMultiplatformExtension::class.java)

class CabretGradleSubplugin : KotlinCompilerPluginSupportPlugin {

    private var gradleExtension : CabretGradleExtension = CabretGradleExtension()

    companion object {
        const val SERIALIZATION_GROUP_NAME = "de.jensklingenberg.cabret"
        const val ARTIFACT_NAME = "cabret-compiler-plugin"
        const val NATIVE_ARTIFACT_NAME = "cabret-compiler-plugin-native"
    }

    override fun apply(target: Project) {

       val extension= target.extensions.create(
            "cabret",
            CabretGradleExtension::class.java
        )
        target.buildDir
        configure(target,extension)

        super.apply(target)
    }

    override fun applyToCompilation(kotlinCompilation: KotlinCompilation<*>): Provider<List<SubpluginOption>> {
        kotlinCompilation.defaultSourceSet {
          //  kotlin.srcDirs("/Users/jklingenberg/Code/MpClient/example/src/generated/kotlin/de/jensklingenberg")
            kotlin.srcDirs(kotlinCompilation.target.project.buildDir.path+"/generated/src/commonMain/kotlin/de/jensklingenberg")
            kotlin.srcDirs(kotlinCompilation.target.project.buildDir.path+"/generated/src/jvmMain/kotlin/de/jensklingenberg")

        }

        gradleExtension = kotlinCompilation.target.project.extensions.findByType(CabretGradleExtension::class.java)
            ?: CabretGradleExtension()
        val project = kotlinCompilation.target.project

        return project.provider {
            val options = mutableListOf<SubpluginOption>(SubpluginOption("enabled", gradleExtension.enabled.toString()))
            options
        }
    }

    private fun configure(project: Project, extension: CabretGradleExtension) {

        val mppExtension: KotlinMultiplatformExtension? = try {
            project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        }catch (exception:Exception){
            null
        }
        if(mppExtension==null){
            return
        }
        val outputDirectory = File(project.buildDir, "mpclient")
        val commonOutputDirectory = File(outputDirectory, "commonMain").also { it.mkdirs() }

        val targets = mppExtension.targets
        val sourceSets = mppExtension.sourceSets

        val outputDirectoryMap = mutableMapOf<TargetName, File>()

        sourceSets.getByName("commonMain").kotlin
                .srcDirs(commonOutputDirectory.toRelativeString(project.projectDir))

        targets.filter { it.name != "metadata" }.forEach { target ->
            val name = "${target.name}Main"
            val sourceSetMain = sourceSets.getByName(name)

            val outDirMain = File(outputDirectory, name).also { it.mkdirs() }

            sourceSetMain.kotlin.srcDirs(outDirMain.toRelativeString(project.projectDir))

            outputDirectoryMap[TargetName(target.name, target.platformType.toKgqlPlatformType())] = outDirMain
        }


    }

    internal fun KotlinPlatformType.toKgqlPlatformType(): PlatformType {
        return when (this) {
            KotlinPlatformType.common -> PlatformType.common
            KotlinPlatformType.jvm -> PlatformType.jvm
            KotlinPlatformType.js -> PlatformType.js
            KotlinPlatformType.androidJvm -> PlatformType.androidJvm
            KotlinPlatformType.native -> PlatformType.native
        }
    }
    /**
     * Just needs to be consistent with the key for CommandLineProcessor#pluginId
     */
    override fun getCompilerPluginId(): String = "cabretPlugin"

    override fun getPluginArtifact(): SubpluginArtifact = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId = ARTIFACT_NAME,
        version = gradleExtension.version // remember to bump this version before any release!
    )

    override fun isApplicable(kotlinCompilation: KotlinCompilation<*>): Boolean {
        return true
    }

    override fun getPluginArtifactForNative() = SubpluginArtifact(
        groupId = SERIALIZATION_GROUP_NAME,
        artifactId = NATIVE_ARTIFACT_NAME,
        version = gradleExtension.version // remember to bump this version before any release!
    )
}