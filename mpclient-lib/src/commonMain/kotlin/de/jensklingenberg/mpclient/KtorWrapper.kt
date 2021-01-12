package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlin.reflect.KClass



class KtorWrapper(val httpClient: HttpClient) {

    suspend inline fun <reified T> get(url: String, headers: List<String> = emptyList()): T {
        return httpClient.request<T> {
            this.method = HttpMethod.Get
            headers.forEach {
                val headi = it.split(":")
                header(headi[0], headi[1])
            }

            url( url)
        }
    }

    suspend inline fun <reified T> post(url: String, bodyData: Any): T {
        return httpClient.post {
            header("", "")
            url(  url)
            contentType(ContentType.Application.Json)
            body = bodyData
        }
    }

    suspend inline fun <reified T> put(url: String): T {
        return httpClient.put(  url)
    }

    suspend inline fun <reified T> delete(url: String): T {
        return httpClient.delete( url)
    }

}