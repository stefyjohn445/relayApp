package com.cristal.ble.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Created by 8017 on 10/24/2017.
 */
object RetrofitClient {

    val DUMMY_USERNAME = "amma"
    val DUMMY_EMAIL = "amma@nederig.com"
    val DUMMY_PASS = "amma123"
    val DUMMY_DEVID = "ABCD"

//    val url = 'http://127.0.0.1:3001/'
    val url = "http://192.168.0.114:3001/"

    fun getClient(baseUrl: String?): Retrofit {

        val gson = GsonBuilder()
            .setLenient()
            .create()

        val builder = OkHttpClient.Builder()
            .readTimeout(30, TimeUnit.SECONDS)
            .connectTimeout(30, TimeUnit.SECONDS)


            val interceptor = HttpLoggingInterceptor()
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY)

            builder.addInterceptor(interceptor)

        val client: OkHttpClient = builder.build()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .client(client)
            .build()
    }
}