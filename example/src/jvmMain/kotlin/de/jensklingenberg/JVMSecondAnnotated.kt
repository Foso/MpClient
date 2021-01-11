package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.Last
import de.jensklingenberg.mpclient.LastImpl
import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.request.*
import io.ktor.http.*
import kotlinx.coroutines.runBlocking


fun main() {

    runBlocking {
        test()
    }
}
suspend fun test1() {
    val tt = HttpClient(CIO) {
        install(JsonFeature)
    }
    val message = tt.post<Post> {
        url("https://jsonplaceholder.typicode.com/posts")
        contentType(ContentType.Application.Json)
        body = Post(1,1,"foo","bar")
    }
    println(message.userId)
}

suspend fun test() {

    val cli = MyHttp(HttpClient(CIO) {
        install(JsonFeature)
    }, baseUrl = "https://jsonplaceholder.typicode.com/")

    val hall = MpClient(cli).createIt<TestApi>(TestApi::class)

    print(hall.postPost(Post(1,1,"foo11","bar")).title)

}


class MpClient(override var httpclient: MyHttp = MyHttp(HttpClient(CIO))) : Last by LastImpl(httpclient){

}
