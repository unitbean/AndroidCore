package com.ub.utils.di.services

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.jakewharton.retrofit2.converter.kotlinx.serialization.asConverterFactory
import com.ub.utils.BuildConfig
import com.ub.utils.di.components.AppScope
import com.ub.utils.di.services.api.responses.PostResponse
import com.ub.utils.download
import io.github.rotbolt.flakerokhttpcore.FlakerInterceptor
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Inject
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.http.GET
import java.util.concurrent.TimeUnit

@Inject
@AppScope
class ApiService(
    json: Json
) {

    private val converter = json.asConverterFactory("application/json".toMediaType())

    private val flakerInterceptor: Interceptor = FlakerInterceptor.Builder().build()

    private val loggingInterceptor: Interceptor = HttpLoggingInterceptor().setLevel(
        if (BuildConfig.DEBUG) HttpLoggingInterceptor.Level.BODY else HttpLoggingInterceptor.Level.NONE
    )

    val api: Api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://jsonplaceholder.typicode.com/")
            .client(httpClient)
            .addConverterFactory(converter)
            .build()

        retrofit.create(Api::class.java)
    }

    private val httpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(flakerInterceptor)
            .addInterceptor(loggingInterceptor)
            .build()
    }

    suspend fun downloadImage(url: String): Bitmap? {
        return httpClient.download(url) {
            return@download BitmapFactory.decodeStream(it)
        }
    }

    interface Api {
        @GET("/posts")
        suspend fun loadPosts() : List<PostResponse>
    }
}