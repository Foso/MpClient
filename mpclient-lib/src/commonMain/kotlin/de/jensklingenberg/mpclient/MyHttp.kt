package de.jensklingenberg.mpclient



class MyHttp(val baseUrl: String = "", val ktorWrapper: KtorWrapper) {

    suspend inline fun <reified T> get(url: String, headers: List<String> = emptyList()): T {
        return ktorWrapper.get(baseUrl + url, headers)
    }

    suspend inline fun <reified T> post(url: String, bodyData: Any): T {
        return ktorWrapper.post(baseUrl + url, bodyData)
    }

    suspend inline fun <reified T> put(url: String): T {
        return ktorWrapper.put(baseUrl + url)
    }

    suspend inline fun <reified T> delete(url: String): T {
        return ktorWrapper.delete(baseUrl + url)
    }



}


