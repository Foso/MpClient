package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.*


import io.ktor.client.*
import io.ktor.client.engine.cio.*

import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking

class MyClientImpl : MyClient {
    override suspend fun <T> found(url: String): DataHolder<T> {
        val string = "GEHEIM"

        return DataHolder(string)

    }

}




suspend fun test() {

    val cli = MyHttp(baseUrl = "https://jsonplaceholder.typicode.com/").apply {
        myClient = MyClientImpl()
        ktorWrapper = KtorWrapper(HttpClient(CIO) {
            install(JsonFeature)
        })
    }

    /**
     *  val hall = cli.dodo<TestApi>()

    println(hall.postPost(Post(1,1,"foo11","bar")).title)
    println(hall.getPosts().size)
    println(hall.getPost(4))
    println(hall.getPostsByUserId(1).size)
     */
 //   val test = cli.dodo<TestApi>()

   // println("TST  " + test.getPosts().size)

}