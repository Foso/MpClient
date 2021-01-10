package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import kotlin.native.concurrent.ThreadLocal
import kotlin.reflect.KClass


@Target(AnnotationTarget.FUNCTION)
annotation class GET(val path:String)


@Target(AnnotationTarget.FUNCTION)
annotation class Provides()

suspend fun getit(url:String): HttpStatement{
    return HttpClient(CIO).get<HttpStatement>(url)

}

@Target(AnnotationTarget.FUNCTION)
annotation class Inject()

interface Last{
    @Inject
    fun <T> createIt(kClass: KClass<*>): T
}