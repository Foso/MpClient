package de.jensklingenberg


import de.jensklingenberg.model.Post
import de.jensklingenberg.mpclient.RestService
import de.jensklingenberg.mpclient.http.BODY
import de.jensklingenberg.mpclient.http.GET
import de.jensklingenberg.mpclient.http.PATH
import de.jensklingenberg.mpclient.http.POST

interface TestApi : RestService {

    @GET("posts")
    suspend fun getPosts(): List<Post>

    @GET("posts/{userId}")
    suspend fun getPost(@PATH("userId")myUserId: Int = 4): Post

    @POST("posts")
    suspend fun postPost(@BODY myUserId: Post): Post

    suspend fun getPosts2(userId: Int = 4, name: String = "Hallo"): Map<String,Post>

}

