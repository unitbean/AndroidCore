package com.ub.utils.ui.main

import android.graphics.Bitmap
import com.ub.utils.containsIgnoreCase
import com.ub.utils.di.components.MainScope
import com.ub.utils.di.services.api.responses.PostResponse
import com.ub.utils.getMyPublicIp
import me.tatarka.inject.annotations.Inject
import java.net.InetAddress
import java.util.*

@Inject
@MainScope
class MainInteractor(private val repository: IMainRepository) {

    suspend fun loadPosts(): List<PostResponse> {
        return repository.getPosts()
    }

    suspend fun myIp(): String {
        return getMyPublicIp().fold(
            onSuccess = InetAddress::toString,
            onFailure = { throwable: Throwable -> throwable.message ?: throwable::class.java.simpleName }
        )
    }

    fun generatePushContent(list: List<PostResponse>): Pair<String, String> {
        val rnd = list[Random().nextInt(list.size)]
        return Pair(rnd.title, rnd.body)
    }

    suspend fun loadImage(url: String): Bitmap {
        val image = repository.getImage(url) ?: throw IllegalStateException("Image is null")

        return image
    }
}