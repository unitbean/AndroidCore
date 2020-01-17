package com.ub.utils.ui.main

import android.graphics.Bitmap
import com.ub.utils.BaseApplication
import com.ub.utils.di.services.ApiService
import com.ub.utils.di.services.api.responses.PostResponse
import io.reactivex.Single
import javax.inject.Inject

interface IMainRepository {

    fun getPosts(): Single<List<PostResponse>>
    suspend fun getImage(url: String): Bitmap?
}

class MainRepository : IMainRepository {

    @Inject lateinit var api : ApiService

    init {
        BaseApplication.appComponent.inject(this)
    }

    override fun getPosts(): Single<List<PostResponse>> {
        return api.api.loadPosts()
    }

    override suspend fun getImage(url: String): Bitmap? {
        return api.downloadImage(url)
    }
}