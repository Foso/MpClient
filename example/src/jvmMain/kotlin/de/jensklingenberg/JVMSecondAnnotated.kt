package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.Call
import de.jensklingenberg.mpclient.CallAdapter
import de.jensklingenberg.mpclient.KtorWrapper
import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass


fun main() {




    runBlocking {

        mutableListOf<Post>()
        test<Flow<List<Post>>, String>()

        val http = HttpClient(CIO) {
            install(JsonFeature)
        }

        val cli = MyHttp(baseUrl = "https://jsonplaceholder.typicode.com/").apply {
            ktorWrapper = KtorWrapper(HttpClient(CIO) {
                install(JsonFeature)
            })
        }

        val api = cli.create<TestApi>()

        val posts: Flow<List<Post>> = api.getFlowPosts()
        posts.collect {
            print(it.first())
        }

    }
}


inline fun <reified T, P> test() {
    // println(T::class.java.cast(String))
}




