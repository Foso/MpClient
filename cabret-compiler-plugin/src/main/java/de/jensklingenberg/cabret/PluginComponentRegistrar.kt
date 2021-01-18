package de.jensklingenberg.cabret

import com.google.auto.service.AutoService
import de.jensklingenberg.cabret.compiler.CabretIrGenerationExtension
import kastree.ast.Node
import kastree.ast.psi.Parser
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.cli.common.CLIConfigurationKeys
import org.jetbrains.kotlin.cli.common.config.addKotlinSourceRoot
import org.jetbrains.kotlin.cli.common.config.kotlinSourceRoots
import org.jetbrains.kotlin.cli.common.messages.MessageCollector
import org.jetbrains.kotlin.cli.jvm.compiler.createSourceFilesFromSourceRoots

import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.project.Project
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.psi.*
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

@AutoService(ComponentRegistrar::class)
class PluginComponentRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        if (configuration[KEY_ENABLED] == false) {
            return
        }
        val myHttpClientArgName = "httpClient"
        val myHttpClientClassName = "MyHttp"
        val myHttpPackage = "de.jensklingenberg.mpclient.$myHttpClientClassName"

        val sourceFiles = createSourceFilesFromSourceRoots(configuration, project, configuration.kotlinSourceRoots)
        sourceFiles.forEach { ktFile ->
            ktFile.children.filterIsInstance<KtClass>().forEach { ktClass ->
                ktClass.getSuperTypeList()?.entries?.forEach {
                    if (ktClass.isInterface() && (it.text == "RestService")) {
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
                                }else if(requestAnno.size>1){
                                    errorFound = true
                                    errorMessage = "Only one HTTP method is allowed. Found: "+requestAnno.joinToString(separator =  " and ") { it::class.simpleName?:"" }
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

                                val body = if (errorFound) {
                                    """ throw IllegalArgumentException("$errorMessage") """
                                } else {

                                    val requestFuncName = when (myRequestAnno) {
                                        is MyAnnotation.Request.GET -> """ return $myHttpClientArgName.get("$pathUrl") """
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

                                funcText += """
                                override suspend fun ${myFunc.name}$funcParamsText: ${myFunc.returnType.name} {
                                    $body
                                }                          
                                """

                            }

                        val testClass = """
                            package de.jensklingenberg
                
                            $imports
                            import $myHttpPackage
                            
                            fun api2($myHttpClientArgName: $myHttpClientClassName) = object : ${ktClass.name} {
                                $funcText
                            }
                        """.trimIndent()

                        val secTemp = Files.createTempDirectory("mytemp")
                        val tempFile = File(secTemp.toAbsolutePath().toString() + "/temp.kt").writeText(testClass)
                        configuration.addKotlinSourceRoot(secTemp.toAbsolutePath().toString() + "/temp.kt", true)



                        val extFun = """ 
package example.commonMain

import de.jensklingenberg.TestApi
import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.features.*
import kotlin.reflect.KClass

            
class _TestApiImpl(val myHttp: MyHttp): TestApi{
    override suspend fun getPosts(): List<Post> {
        return myHttp.get("posts")
    }

    override suspend fun getPost(myUserId: Int): Post {
        TODO("Not yet implemented")
    }

    override suspend fun postPost(otherID: Post): Post {
        TODO("Not yet implemented")
    }

    override suspend fun getPostsByUserId(myUserId: Int): List<Post> {
        TODO("Not yet implemented")
    }
}           
            
            
inline fun <reified T> MyHttp.dodo() : T {
    return when(T::class.qualifiedName){
        TestApi::class.qualifiedName->{
            return _TestApiImpl(this) as T
        }
        else -> {
            throw NotImplementedError()
        }
    }
}

class Hallo
            
        """.trimIndent()

                        File("/Users/jklingenberg/Code/MpClient/example/build/generated/kotlin/de/jensklingenberg/").mkdir()
                        File("/Users/jklingenberg/Code/MpClient/example/build/generated/kotlin/de/jensklingenberg/Hallo.kt").writeText(extFun)
                        configuration.addKotlinSourceRoot("/Users/jklingenberg/Code/MpClient/example/build/generated/kotlin/de/jensklingenberg/Hallo.kt", true)

                    }
                }
            }
        }



        val messageCollector = configuration.get(CLIConfigurationKeys.MESSAGE_COLLECTOR_KEY, MessageCollector.NONE)


        IrGenerationExtension.registerExtension(project, CabretIrGenerationExtension(messageCollector))

    }

    private fun getImportStatements(ktFile: KtFile) =
        ktFile.importList?.children?.joinToString(separator = "\n") { it.text }
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