package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.KtorWrapper
import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.runBlocking


fun main() {



    runBlocking {
        test<Flow<List<Post>>,String>()


        val cli = MyHttp(baseUrl = "https://jsonplaceholder.typicode.com/").apply {
            ktorWrapper = KtorWrapper(HttpClient(CIO) {
                install(JsonFeature)
            })
        }

        val api = cli.dodo<TestApi>()
        val posts = api.getFlowPosts()
        posts.collect {
            print(it.first())
        }

    }
}

suspend fun test(cli: MyHttp) {


    val api = cli.get<List<Post>>("posts")

    println(api.size)
    simple(cli)

}

inline fun <reified T,P> test(){
   // println(T::class.java.cast(String))
}

fun simple(cli: MyHttp): Flow<List<Post>> {


    return flow<List<Post>> {
        emit(cli.get<List<Post>>("posts"))
    }
}


