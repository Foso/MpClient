package de.jensklingenberg.cabret

import com.google.auto.service.AutoService
import de.jensklingenberg.cabret.compiler.CabretIrGenerationExtension
import kastree.ast.Node
import kastree.ast.psi.Parser
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.serialization.IrFileSerializer
import org.jetbrains.kotlin.backend.common.serialization.proto.IrFileOrBuilder
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.psi.PsiFile
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.container.StorageComponentContainer
import org.jetbrains.kotlin.container.useInstance
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.ModuleDescriptor
import org.jetbrains.kotlin.extensions.CollectAdditionalSourcesExtension
import org.jetbrains.kotlin.extensions.DeclarationAttributeAltererExtension
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.ir.builders.IrBuilder
import org.jetbrains.kotlin.js.sourceMap.SourceFilePathResolver
import org.jetbrains.kotlin.platform.TargetPlatform
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi2ir.Psi2IrTranslator
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files

@AutoService(ComponentRegistrar::class)
class PluginComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {

        val testClass = """
            package de.jensklingenberg

            import io.ktor.client.*
            import io.ktor.client.request.*
            import de.jensklingenberg.mpclient.MyHttp
            
            interface StubInterface{
                fun funStub()
            }
            
            class StubClass() : StubInterface{
            
            override fun funStub(){}
            }
            
            fun api2(httpClient: MyHttp) = object : TestApi {
            
                override suspend fun getPosts(): List<Post> {
                    return httpClient.get("https://jsonplaceholder.typicode.com/posts")
                }
            }
        """.trimIndent()


//(testit.get(1).decls.get(0) as Node.Decl.Structured).parents.filterIsInstance<Node.Decl.Structured.Parent.Type>().any { it.type.pieces.any { it.name=="RestService" } }
        //(testit.get(1).decls).filterIsInstance<Node.Decl.Structured>().filter { it.parents.filterIsInstance<Node.Decl.Structured.Parent.Type>().any { it.type.pieces.any { it.name=="RestService" } } }
        val test = createSourceFilesFromSourceRoots(configuration, project, configuration.kotlinSourceRoots)
        SourceFilePathResolver(listOf(File(configuration.kotlinSourceRoots.get(0).path)))
        KtPsiFactory(project, true).createFile(File(configuration.kotlinSourceRoots.get(0).path).readText())
        val testit = configuration.kotlinSourceRoots.map { readFile(File(it.path)) }
        val allFilesWithRestService = testit.filter {
            it.decls.filterIsInstance<Node.Decl.Structured>().any {
                it.parents.filterIsInstance<Node.Decl.Structured.Parent.Type>()
                    .any { it.type.pieces.any { it.name == "RestService" } }
            }
        }
        val funcs =
            (allFilesWithRestService.get(0).decls.get(0) as Node.Decl.Structured).members.filterIsInstance<Node.Decl.Func>()
        val annoSets = funcs.get(0).mods.filterIsInstance<Node.Modifier.AnnotationSet>()
            .first().anns.firstOrNull() { it.names.any { it == "GET" } }

//        val bb =  (testit[0].decls[3] as Node.Decl.Structured).members.filterIsInstance<Node.Decl.Func>()
        //    val cc = bb.filter { it.mods.any() { (it as? Node.Modifier.AnnotationSet)?.anns?.any { it.names.contains("Provides") } ==true } }

        val isit = true


        if (isit) {
            val secTemp = Files.createTempDirectory("install")
            val tempFile = File(secTemp.toAbsolutePath().toString() + "/temp.kt").writeText(testClass)
            // val tempDir = Files.createTempDirectory("/Users/jklingenberg/Code/MpClient/mpclient-lib/src/commonMain/kotlin/de/jensklingenberg/mpclient")

            //File("/Users/jklingenberg/Code/MpClient/mpclient-lib/src/commonMain/kotlin/de/jensklingenberg/mpclient/MyTestFile.kt").writeText(testClass)
            configuration.addKotlinSourceRoot(secTemp.toAbsolutePath().toString() + "/temp.kt", true)
        }

//File(secTemp.toAbsolutePath().toString()+"/temp.kt").writeText(testClass)


        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)


        IrGenerationExtension.registerExtension(project, CabretIrGenerationExtension(messageCollector))

    }
}

fun readFile(file: File): Node.File {
    val fileText = file.bufferedReader()
        .use(BufferedReader::readText)
    return Parser.parseFile(fileText)
}