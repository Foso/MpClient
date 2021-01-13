package de.jensklingenberg.mpclient

import io.ktor.client.*

import kotlin.reflect.KClass

class LastImpl(override var httpclient: MyHttp = MyHttp().apply { ktorWrapper = KtorWrapper(HttpClient()) }) : Last {

    fun MyClient(){

    }

    override fun <T> createIt(kClass: KClass<*>): T {
        return MyInjector()

    }

    override var baseUrl: String=""

}


fun <T> MyInjector(): T {
    throw NotImplementedError("An operation is not implemented: ")
}

interface RestService