package com.ub.utils.ui.main

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.ViewModel
import com.ub.utils.*
import com.ub.utils.di.services.api.responses.PostResponse
import com.ub.security.AesGcmEncryption
import com.ub.security.AuthenticatedEncryption
import com.ub.security.toSecretKey
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import java.util.*

@AssistedFactory
interface MainViewModelProvider {
    fun create(urlToLoad: String): MainViewModel
}

class MainViewModel @AssistedInject constructor(
    @Assisted private val urlToLoad: String,
    private val interactor: MainInteractor,
) : ViewModel() {

    private val list = ArrayList<PostResponse>()

    private val _done = MutableSharedFlow<Unit>()
    val done = _done.asSharedFlow()

    private val _showPush = MutableSharedFlow<Pair<String, String>>()
    val showPush = _showPush.asSharedFlow()

    private val _connectivity = MutableSharedFlow<String>()
    val connectivity = _connectivity.asSharedFlow()

    private val _isEquals = MutableSharedFlow<Boolean>()
    val isEquals = _isEquals.asSharedFlow()

    private val _image = MutableSharedFlow<Bitmap>()
    val image = _image.asSharedFlow()

    fun load() {
        withUseCaseScope(
            onError = { e -> LogUtils.e("POST", e.message ?: "Error", e) }
        ) {
            val posts = interactor.loadPosts()
            list.renew(posts)
            delay(100)
            _done.emit(Unit)
        }
    }

    fun generatePushContent() {
        withUseCaseScope {
            val push = interactor.generatePushContent(list)
            _showPush.emit(push)
        }
    }

    fun networkTest(context: Context) {
        withUseCaseScope(
            onError = { e -> LogUtils.e("NetworkTest", e.message, e) }
        ) {
            val network = CNetwork(context, "https://google.com")
            network.startListener().collect {
                _connectivity.emit(it)
            }
        }
    }

    fun isEquals() {
        withUseCaseScope {
            val isEquals = interactor.isEquals()
            _isEquals.emit(isEquals)
        }
    }

    fun loadImage() {
        withUseCaseScope(
            onError = { e -> LogUtils.e("ImageDownload", e.message, e) }
        ) {
            val url = testAes(urlToLoad)
            val image = interactor.loadImage(url)
            _image.emit(image)
        }
    }

    private fun testAes(textToTest: String): String {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            LogUtils.d("AES/GCM", "Encryption test started")
            val startTime = SystemClock.uptimeMillis()
            val key = "test".toSecretKey()
            val encryption: AuthenticatedEncryption = AesGcmEncryption()
            val encrypted = encryption.encrypt(
                urlToLoad.toByteArray(charset("UTF-8")),
                key
            )
            val encryptTime = SystemClock.uptimeMillis()
            LogUtils.d("AES/GCM", "Time to encrypt is ${encryptTime - startTime}")
            val decryption: AuthenticatedEncryption = AesGcmEncryption()
            val decrypted = decryption.decrypt(
                encrypted,
                key
            )
            val decryptTime = SystemClock.uptimeMillis()
            LogUtils.d("AES/GCM", "Time to decrypt is ${decryptTime - encryptTime}")
            return String(decrypted)
        } else textToTest
    }
}