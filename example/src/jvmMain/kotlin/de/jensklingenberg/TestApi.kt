package de.jensklingenberg

import de.jensklingenberg.mpclient.GET
import de.jensklingenberg.mpclient.MyHttp
import de.jensklingenberg.mpclient.RestService
import io.ktor.client.request.*

interface TestApi: RestService {

    @GET("Heydu")
    suspend fun getPosts(): List<Post>
}

class Testi(val http: MyHttp) : TestApi{
    override suspend fun getPosts(): List<Post> {
        return http.httpClient.get("dddd")
    }

}