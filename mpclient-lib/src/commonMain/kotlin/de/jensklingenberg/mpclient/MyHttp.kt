package de.jensklingenberg.mpclient

import io.ktor.client.*
import io.ktor.client.request.*

class MyHttp(val httpClient: HttpClient){

    suspend inline fun <reified T> get(url:String) :T {
        return httpClient.get(url)
    }
}