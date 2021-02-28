package de.jensklingenberg.cabret

import com.google.auto.service.AutoService
import de.jensklingenberg.cabret.compiler.CabretIrGenerationExtension
import kastree.ast.Node
import kastree.ast.psi.Parser
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.KotlinSourceRoot
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots
import org.jetbrains.kotlin.cli.jvm.compiler.report

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.com.intellij.openapi.vfs.StandardFileSystems
import org.jetbrains.kotlin.com.intellij.openapi.vfs.VirtualFileManager
import org.jetbrains.kotlin.com.intellij.psi.PsiManager
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.CollectAdditionalSourcesExtension
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.allChildren
import java.io.BufferedReader
import java.io.File
import java.nio.file.Files

sealed class MyAnnotation {
    sealed class Request(open val path: String) : MyAnnotation() {
        class GET(override val path: String) : Request(path)
        class POST(override val path: String) : Request(path)
    }

    sealed class Param(open val path: String) : MyAnnotation() {
        class Body() : Param("")
        class Path(override val path: String) : Param("")
    }
}

data class MyType(val name: String)

data class MyParam(val name: String, val type: MyType)

data class MyFunction(val name: String, val returnType: MyType, val isSuspend: Boolean = false)

fun File.toKtFile(project: Project): KtFile {
    val fileSystem =
        VirtualFileManager.getInstance().getFileSystem(StandardFileSystems.FILE_PROTOCOL)
    val psiManager = PsiManager.getInstance(project)

    return (fileSystem.findFileByPath(absolutePath)
        ?: error("can't fine virtual file : $absolutePath"))
        .let { psiManager.findFile(it) ?: error("can't fine psi file : $absolutePath") }
        .let { KtFile(it.viewProvider, false) }
}

@AutoService(ComponentRegistrar::class)
class PluginComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val buildPath = "/Users/jklingenberg/Code/MpClient/example/build/"
        val generatedPath = buildPath + "generated/src/jvmMain/kotlin/de/jensklingenberg/"
        val generatedPathCommon = buildPath + "generated/src/commonMain/kotlin/de/jensklingenberg/"

        val myHttpClientArgName = "httpClient"
        val myHttpClientClassName = "MyHttp"
        val myHttpPackage = "de.jensklingenberg.mpclient.$myHttpClientClassName"

        val sourceFiles = createSourceFilesFromSourceRoots(configuration, project, configuration.kotlinSourceRoots)
        val annotatedClass = mutableListOf<String>()

        sourceFiles.forEach { ktFile ->
            ktFile.children.filterIsInstance<KtClass>().forEach { ktClass ->
                ktClass.getSuperTypeList()?.entries?.forEach {
                    if (ktClass.isInterface() && (it.text == "RestService")) {
                        annotatedClass.add(ktClass.name!!)
                        val imports = getImportStatements(ktFile)

                        var funcText = ""
                        ktClass.children.filterIsInstance<KtClassBody>()
                            .first().children.filterIsInstance<KtNamedFunction>().forEach {
                                var errorFound = false
                                var errorMessage = ""
                                var bodyDataName = ""
                                val funcAnnos = it.getMyAnnos()


                                val requestAnno: List<MyAnnotation.Request> =
                                    funcAnnos.filterIsInstance<MyAnnotation.Request>()

                                if (requestAnno.isEmpty()) {
                                    errorFound = true
                                    errorMessage = "HTTP method annotation is required (e.g., @GET, @POST, etc.)."
                                } else if (requestAnno.size > 1) {
                                    errorFound = true
                                    errorMessage =
                                        "Only one HTTP method is allowed. Found: " + requestAnno.joinToString(separator = " and ") {
                                            it::class.simpleName ?: ""
                                        }
                                }
                                val myRequestAnno = requestAnno.first()

                                val bodyAnnoL: MutableList<MyAnnotation.Param.Body> = mutableListOf()
                                var pathUrl = (requestAnno.first().path ?: "")

                                var funcParamsText = "(" + it.valueParameters.joinToString(separator = ",") {

                                    val paramName = it.name ?: ""
                                    val type = it.text.substringAfter(":").substringBefore("=")
                                    val paramsAnno = it.getMyParamAnno()
                                    paramsAnno.filterIsInstance<MyAnnotation.Param.Path>().forEach {
                                        pathUrl = pathUrl.replace("{${it.path}}", "\$$paramName")
                                    }

                                    if (myRequestAnno is MyAnnotation.Request.POST) {

                                        paramsAnno.filterIsInstance<MyAnnotation.Param.Body>().firstOrNull()?.let {
                                            bodyDataName = paramName
                                            bodyAnnoL.add(it)
                                        }
                                    }

                                    "$paramName : $type"
                                }
                                if (bodyAnnoL.toList().size > 1) {
                                    errorFound = true
                                    errorMessage = "Multiple @Body method annotations found"
                                }

                                funcParamsText += ")"

                                val returType = it.text.substringAfterLast(":")

                                val funName = it.name ?: ""

                                val myFunc = MyFunction(funName, MyType(returType), isSuspend = true)
                                it.isSupendFun()
                                val body = if (errorFound) {
                                    """ throw IllegalArgumentException("$errorMessage") """
                                } else {

                                    val requestFuncName = when (myRequestAnno) {
                                        is MyAnnotation.Request.GET -> {
                                            //Flow<List<Post>>
                                            if (nestedType(myFunc.returnType.name) && !it.isSupendFun()) {
                                                """ return $myHttpClientArgName.supget<${myFunc.returnType.name},${
                                                    subType(
                                                        myFunc.returnType.name
                                                    )
                                                }>("$pathUrl") """
                                            } else {
                                                """ return $myHttpClientArgName.get("$pathUrl") """
                                            }


                                        }
                                        is MyAnnotation.Request.POST -> {
                                            when (bodyAnnoL) {
                                                null -> {
                                                    """ throw IllegalArgumentException("@POST found without a @Body") """
                                                }
                                                else -> {

                                                    """ return $myHttpClientArgName.post("$pathUrl",$bodyDataName) """
                                                }
                                            }

                                        }
                                    }
                                    requestFuncName
                                }

                                val suspendKeywordText = if (it.isSupendFun()) {
                                    "suspend"
                                } else {
                                    ""
                                }

                                funcText += """
override $suspendKeywordText fun ${myFunc.name}$funcParamsText: ${myFunc.returnType.name} {
    $body
}                          
                                """

                            }

                        val extFun3 = """ 
//Generated by MpClient      
package de.jensklingenberg

$imports
import $myHttpPackage
//import de.jensklingenberg.model.Post
import kotlinx.coroutines.flow.flow            
class _${ktClass.name}Impl(val $myHttpClientArgName: $myHttpClientClassName): ${ktClass.name}{
    
    $funcText
    
                }           
          
            
        """.trimIndent()
                        val generateSources = true

                        val genPath: String = if (generateSources) {
                            generatedPathCommon
                        } else {
                            Files.createTempDirectory("mytemp").toAbsolutePath().toString()
                        }

                        File(genPath + "/_${ktClass.name}Impl.kt").writeText(extFun3)
                        configuration.addKotlinSourceRoot(
                            genPath + "/_${ktClass.name}Impl.kt",
                            true
                        )


                    }
                }
            }


        }

        val serviceClassImport = annotatedClass.joinToString(separator = "\n") {
            "import de.jensklingenberg.${it}\n import de.jensklingenberg._${it}Impl"
        }

        val whenBody = annotatedClass.joinToString(separator = "\n") {
            "${it}::class.qualifiedName->{\n" +
                    "          _${it}Impl(this) as T\n" +
                    "}"
        }

        val extFun = """ 
//Generated by MpClient            
package de.jensklingenberg

$serviceClassImport

import de.jensklingenberg.mpclient.MyHttp
                                      
inline fun <reified T> MyHttp.create() : T {
   return when(T::class.qualifiedName){
        $whenBody
        
        else -> {
            throw NotImplementedError()
        }
    }
}
            
        """.trimIndent()

        File(generatedPath).mkdirs()
        val hallo =
            File(generatedPath + "MpClientExt.kt")
        hallo.createNewFile()
        hallo.writeText(extFun)
        if (true){//configuration.kotlinSourceRoots.any { it.path == hallo.path }) {
            configuration.addKotlinSourceRoot(
                generatedPath + "MpClientExt.kt",
                true
            )
        }


        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)


        IrGenerationExtension.registerExtension(project, CabretIrGenerationExtension(messageCollector))

    }

    private fun nestedType(name: String): Boolean {
        return name.contains("<") && subType(name).isNotEmpty()
        //Flow<List<Post>>

    }

    private fun subType(name: String): String {
        return name.substringAfter("<").substringBeforeLast(">")
        //Flow<List<Post>>

    }

    private fun getImportStatements(ktFile: KtFile) =
        ktFile.importList?.children?.joinToString(separator = "\n") { it.text }
}

private fun KtNamedFunction.isSupendFun(): Boolean {
    return this.modifierList?.allChildren?.any { it.text == "suspend" } ?: false
}

private fun KtParameter.getMyParamAnno(): List<MyAnnotation.Param> {
    val annos = mutableListOf<MyAnnotation.Param>()
    this.annotationEntries.forEach {
        val annoName = it.text.replace("@", "").substringBefore("(")//it.shortName?.identifier
        when (annoName) {
            "Path" -> {
                it.valueArguments.forEach {
                    val pathValue =
                        it.getArgumentExpression()?.text?.substringAfter("\"")?.substringBeforeLast("\"") ?: ""
                    annos.add(MyAnnotation.Param.Path(pathValue))
                }
            }
            "Body" -> {
                annos.add(MyAnnotation.Param.Body())
            }
        }
    }

    return annos
}


private fun KtNamedFunction.getMyAnnos(): List<MyAnnotation> {
    val annos = mutableListOf<MyAnnotation>()
    this.annotationEntries.forEach {
        val annoName = it.text.replace("@", "").substringBefore("(")//it.shortName?.identifier
        when (annoName) {
            "GET" -> {
                it.valueArguments.forEach {
                    val pathValue =
                        it.getArgumentExpression()?.text?.substringAfter("\"")?.substringBeforeLast("\"") ?: ""
                    annos.add(MyAnnotation.Request.GET(pathValue))
                }
            }
            "POST" -> {
                it.valueArguments.forEach {
                    val pathValue =
                        it.getArgumentExpression()?.text?.substringAfter("\"")?.substringBeforeLast("\"") ?: ""
                    annos.add(MyAnnotation.Request.POST(pathValue))
                }
            }

        }
    }

    return annos
}


private fun Node.Decl.Func.hasGETAnnotation(): MyAnnotation.Request.GET? {
    val anno = this.hasAnnotation("GET")
    return if (anno != null) {
        val pathValue =
            ((anno.args[0].expr as? Node.Expr.StringTmpl)?.elems?.get(0) as Node.Expr.StringTmpl.Elem.Regular).str
        MyAnnotation.Request.GET(pathValue)
    } else {
        null
    }

}


private fun Node.Decl.Func.hasAnnotation(s: String): Node.Modifier.AnnotationSet.Annotation? {
    return mods.filterIsInstance<Node.Modifier.AnnotationSet>()
        .firstOrNull()?.anns?.firstOrNull() { it.names.any { it == s } }
}

fun readFile(file: File): Node.File {
    val fileText = file.bufferedReader()
        .use(BufferedReader::readText)
    return Parser.parseFile(fileText)
}