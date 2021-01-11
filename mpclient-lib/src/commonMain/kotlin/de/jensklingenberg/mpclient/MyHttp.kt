package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.http.*

class MyHttp(val httpClient: HttpClient, val baseUrl: String = "") {

    suspend inline fun <reified T> get(url: String): T {
        return httpClient.get<T>(baseUrl + url)
    }

    suspend inline fun <reified T> post(url: String,bodyData:Any): T {
        return httpClient.post<T> {
            url(url)
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