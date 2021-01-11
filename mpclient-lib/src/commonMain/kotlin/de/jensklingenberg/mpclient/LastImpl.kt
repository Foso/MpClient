package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import kotlin.reflect.KClass

class LastImpl(override var httpclient: MyHttp = MyHttp(HttpClient(CIO))) : Last {


    override fun <T> createIt(kClass: KClass<*>): T {
        return MyInjector()

    }

    override var baseUrl: String=""

}


fun <T> MyInjector(): T {
    throw NotImplementedError("An operation is not implemented: ")
}

interface RestService