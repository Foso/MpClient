package de.jensklingenberg


import de.jensklingenberg.mpclient.Inject
import de.jensklingenberg.mpclient.Last
import de.jensklingenberg.mpclient.LastImpl
import de.jensklingenberg.mpclient.MyHttp
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import kotlinx.coroutines.runBlocking
import kotlin.reflect.KClass


fun main() {



    runBlocking {
        test()
    }



}


suspend fun test() {

    val cli =  MyHttp(HttpClient(CIO) {
        install(JsonFeature)
    })

    val hall = MpClient(cli).createIt<TestApi>(TestApi::class)


    print(hall.getPosts().size)

}


class MpClient( var httpclient2: MyHttp = MyHttp(HttpClient(CIO))) : Last by LastImpl(httpclient2) {

    val httpclient = httpclient2



}

