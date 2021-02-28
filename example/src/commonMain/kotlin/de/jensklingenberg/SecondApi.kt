package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.RestService
import de.jensklingenberg.mpclient.http.*
import kotlinx.coroutines.flow.Flow

interface SecondApi : RestService {

    @Headers(value = ["Accept: application/json", "DDDD"])
    @GET("posts")
    suspend fun getPosts(): List<Post>



}

