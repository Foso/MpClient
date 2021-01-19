package de.jensklingenberg.mpclient

import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlin.reflect.KClass


class DataHolder<T>(var any: Any) {

    fun <T> castit(): T {
        return any as T
    }

}

class Call<T>(var any: T) {

    fun castit(): T {
        return any as T
    }

}

abstract class TtClient {

    val ktorWrapper: KtorWrapper? = null

    fun getIT(url: String, headers: List<String> = emptyList()): Call<*> {
        return get(url, headers)
    }

    abstract fun get(url: String, headers: List<String>): Call<*>


}


interface MyClient {
    suspend fun <T> found(url: String): DataHolder<T>
}


class MyHttp(val baseUrl: String = "") {
    var ktorWrapper: KtorWrapper? = null
    var myClient: MyClient? = null


    suspend inline fun <reified T> get(url: String, headers: List<String> = emptyList()): T {
        return ktorWrapper?.get(baseUrl + url, headers)!!
    }

    suspend inline fun <reified T> post(url: String, bodyData: Any): T {
        return ktorWrapper?.post(baseUrl + url, bodyData)!!
    }

    suspend inline fun <reified T> put(url: String): T {
        return ktorWrapper?.put(baseUrl + url)!!
    }

    suspend inline fun <reified T> delete(url: String): T {
        return ktorWrapper?.delete(baseUrl + url)!!
    }


    inline fun <reified T : Any> classOfList(list: Call<T>) = T::class


    inline fun <reified TReturn, reified PRequest> supget(url: String): TReturn {
        //T is return type
        //P is requested type

        val callAdapter = CallAdapter()

        val adapterList = listOf(callAdapter)

        adapterList.firstOrNull { callAdapter.supportedType(TReturn::class) }?.let {
            val datat = it.convert { get<PRequest>(url) }
            return datat as TReturn
        }

        throw NotImplementedError()
    }
}


class CallAdapter {

    fun supportedType(returnType: KClass<*>): Boolean {
        return returnType.qualifiedName == Flow::class.qualifiedName
    }

    fun <T> convert(namee: suspend () -> T): Flow<T> = flow<T> {
        emit(namee())
    }

}

