package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

interface MyClient{
    fun <T> get(url: String) : T
}



class MyHttp(val httpClient: HttpClient, val baseUrl: String = "") {

    suspend inline fun <reified T> get(url: String, headers: List<String> = emptyList()): T {
        return httpClient.get {
            headers.forEach {
                val headi = it.split(":")
                header(headi[0],headi[1])
            }

            url(baseUrl + url)
        }
    }

    suspend inline fun <reified T> post(url: String, bodyData: Any): T {
        return httpClient.post {
            header("","")
            url(baseUrl + url)
            contentType(ContentType.Application.Json)
            body = bodyData
        }
    }

    suspend inline fun <reified T> put(url: String): T {
        return httpClient.put(baseUrl + url)
    }

    suspend inline fun <reified T> delete(url: String): T {
        return httpClient.delete(baseUrl + url)
    }

}