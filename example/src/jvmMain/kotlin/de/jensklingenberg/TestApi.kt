package de.jensklingenberg

import de.jensklingenberg.mpclient.GET
import de.jensklingenberg.mpclient.RestService

interface TestApi: RestService {

    @GET("Heydu")
    suspend fun getPosts(): List<Post>
}