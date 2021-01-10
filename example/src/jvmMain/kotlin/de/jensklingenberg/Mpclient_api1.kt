package de.jensklingenberg

import io.ktor.client.*
import io.ktor.client.request.*

fun mpclient_api1(httpClient: HttpClient) = object : TestApi {

    override suspend fun getPosts(): List<Post> {
        return httpClient.get("/posts")
    }
}