package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.RestService
import de.jensklingenberg.mpclient.http.*

fun main() {

}


interface TestApi : RestService {

    @Headers(value = ["Accept: application/json", "DDDD"])
    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{userId}")
    suspend fun getPost(@Path("userId") myUserId: Int = 4): Post

    @POST("posts")
    suspend fun postPost(@Body otherID: Post): Post


    @GET("posts?userId={userId}")
    suspend fun getPostsByUserId(@Path("userId") myUserId: Int): List<Post>

}

