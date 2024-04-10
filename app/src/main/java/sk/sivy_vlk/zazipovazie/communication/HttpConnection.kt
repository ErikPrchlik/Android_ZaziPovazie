package sk.sivy_vlk.zazipovazie.communication

import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import sk.sivy_vlk.zazipovazie.BuildConfig

object HttpConnection {

    const val URL = BuildConfig.BASE_URL

    fun getClient(): OkHttpClient {
        val logging = HttpLoggingInterceptor()
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        val httpClient: OkHttpClient.Builder = OkHttpClient.Builder()
        httpClient.addInterceptor(logging)
        return httpClient.build()
    }

    fun downloadKMZFile(): Response {
        val request = Request.Builder().url(URL).build()
        return getClient().newCall(request).execute()
    }

}