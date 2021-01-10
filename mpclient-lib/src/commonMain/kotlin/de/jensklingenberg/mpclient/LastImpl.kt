package de.jensklingenberg.mpclient

import io.ktor.client.*
import kotlin.reflect.KClass

class LastImpl(val httpclient: HttpClient) : Last {

    override fun <T> createIt(kClass: KClass<*>): T {
        return MyInjector()

    }

}


fun <T> MyInjector(): T {
    throw NotImplementedError("An operation is not implemented: ")
}

interface RestService