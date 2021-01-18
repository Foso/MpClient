package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.Last
import de.jensklingenberg.mpclient.LastImpl
import de.jensklingenberg.mpclient.MyHttp
import de.jensklingenberg.mpclient.KtorWrapper
import example.commonMain.dodo


import io.ktor.client.*
import io.ktor.client.engine.cio.*

import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking



fun main() {

    runBlocking {
        test()
    }
}

suspend fun test() {

    val cli = MyHttp(ktorWrapper = KtorWrapper(HttpClient(CIO) {
        install(JsonFeature)
    }), baseUrl = "https://jsonplaceholder.typicode.com/")

cli.dodo<TestApi>()


}




