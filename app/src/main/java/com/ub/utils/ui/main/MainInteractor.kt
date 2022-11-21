package com.ub.utils.ui.main

import android.graphics.Bitmap
import com.ub.utils.containsIgnoreCase
import com.ub.utils.di.services.api.responses.PostResponse
import java.util.*

class MainInteractor(private val repository: IMainRepository) {

    suspend fun loadPosts(): List<PostResponse> {
        return repository.getPosts()
    }

    fun isEquals(): Boolean {
        val list = arrayListOf("Test", "TEst", "TESt", "TEST")
        return list.containsIgnoreCase("test")
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