package com.ub.utils.ui.main

import android.graphics.Bitmap
import com.ub.utils.di.components.AppScope
import com.ub.utils.di.services.ApiService
import com.ub.utils.di.services.api.responses.PostResponse
import me.tatarka.inject.annotations.Inject

interface IMainRepository {
    suspend fun getPosts(): List<PostResponse>
    suspend fun getImage(url: String): Bitmap?
}

@Inject
@AppScope
class MainRepository(private val api : ApiService) : IMainRepository {

    override suspend fun getPosts(): List<PostResponse> {
        return api.api.loadPosts()
    }

    override suspend fun getImage(url: String): Bitmap? {
        return api.downloadImage(url)
    }
}