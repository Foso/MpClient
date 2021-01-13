package de.jensklingenberg.mpclient

interface MMClient {

}

class DataHolder<T>(var any: Any) {

    fun <T> castit(): T {
        return any as T
    }

}

interface MyClient {
    suspend fun <T> found(url: String): DataHolder<T>
}

class Holder<T>(var any: Any)

class MyHttp(val baseUrl: String = "") {
    var ktorWrapper: KtorWrapper? = null
    var myClient: MyClient?=null


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

}


