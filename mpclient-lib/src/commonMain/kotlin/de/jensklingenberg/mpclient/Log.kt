package de.jensklingenberg.mpclient

import kotlin.reflect.KClass

@Target(AnnotationTarget.FUNCTION)
annotation class Inject()

interface Last{
    var httpclient: MyHttp

    @Inject
    fun <T> createIt(kClass: KClass<*>): T

    var baseUrl : String


}

