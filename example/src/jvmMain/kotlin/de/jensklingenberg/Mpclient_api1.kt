package de.jensklingenberg

import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.request.*

fun mpclient_api1(httpClient: MyHttp) = object : TestApi {

    override suspend fun getPosts(): List<Post> {
        return httpClient.httpClient.get("/posts")
    }
}