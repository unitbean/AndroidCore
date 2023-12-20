package com.ub.utils.ui.main

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import com.ub.security.AesGcmEncryption
import com.ub.security.AuthenticatedEncryption
import com.ub.security.toSecretKey
import com.ub.utils.BaseApplication
import com.ub.utils.CNetwork
import com.ub.utils.NetworkSpec
import com.ub.utils.createFileWithContent
import com.ub.utils.deleteFile
import com.ub.utils.di.services.api.responses.PostResponse
import com.ub.utils.getFileNameFromUri
import com.ub.utils.getImage
import com.ub.utils.renew
import com.ub.utils.withUseCaseScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import me.tatarka.inject.annotations.Assisted
import me.tatarka.inject.annotations.Inject
import timber.log.Timber

@Inject
class MainViewModel(
    @Assisted private val urlToLoad: String,
    private val interactor: MainInteractor,
    application: Application
) : AndroidViewModel(application) {

    private val list = ArrayList<PostResponse>()

    private val _done = MutableSharedFlow<Unit>()
    val done = _done.asSharedFlow()

    private val _showPush = MutableSharedFlow<Pair<String, String>>()
    val showPush = _showPush.asSharedFlow()

    val connectivity = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        CNetwork(application).specFlow
    } else MutableStateFlow(NetworkSpec.Disabled)

    private val _isEquals = MutableSharedFlow<Boolean>()
    val isEquals = _isEquals.asSharedFlow()

    private val _image = MutableSharedFlow<Bitmap>()
    val image = _image.asSharedFlow()

    init {
        load()
        loadImage()
    }

    private fun load() {
        withUseCaseScope(
            onError = { e -> Timber.e(e, "POST %s", e.message ?: "Error") }
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

    fun cachePickedImage(uri: Uri) {
        withUseCaseScope(
            onError = { e -> Timber.e(e, "CacheImage %s", e.message) }
        ) {
            val context = getApplication<BaseApplication>()
            val nameWithExtension = context.getFileNameFromUri(uri)
            val savedFile = createFileWithContent(
                inputContent = context.contentResolver.openInputStream(uri) ?: return@withUseCaseScope,
                nameWithExtension = nameWithExtension ?: return@withUseCaseScope,
                folder = context.cacheDir
            )
            val bitmap = context.getImage(Uri.fromFile(savedFile))
            _image.emit(bitmap)
        }
    }

    fun isEquals() {
        withUseCaseScope {
            val isEquals = interactor.isEquals()
            _isEquals.emit(isEquals)
        }
    }

    fun removeCachedFiles() {
        val context = getApplication<BaseApplication>()
        val removedList = context.cacheDir?.listFiles()?.map { file ->
            context.deleteFile(file, "${context.packageName}.core.fileprovider")
        }
        removedList?.firstOrNull { false }?.let {
            Timber.e("RemoveCached %s", "Cached files was not be deleted completely")
        }
    }

    private fun loadImage() {
        withUseCaseScope(
            onError = { e -> Timber.e(e, "ImageDownload %s", e.message) }
        ) {
            val url = testAes(urlToLoad)
            val image = interactor.loadImage(url)
            _image.emit(image)
        }
    }

    private fun testAes(textToTest: String): String {
        Timber.d("AES/GCM %s", "Encryption test started")
        val startTime = SystemClock.uptimeMillis()
        val key = "test".toSecretKey()
        val encryption: AuthenticatedEncryption = AesGcmEncryption()
        val encrypted = encryption.encrypt(
            urlToLoad.toByteArray(charset("UTF-8")),
            key
        )
        val encryptTime = SystemClock.uptimeMillis()
        Timber.d("AES/GCM %s", "Time to encrypt is ${encryptTime - startTime}")
        val decryption: AuthenticatedEncryption = AesGcmEncryption()
        val decrypted = decryption.decrypt(
            encrypted,
            key
        )
        val decryptTime = SystemClock.uptimeMillis()
        Timber.d("AES/GCM %s", "Time to decrypt is ${decryptTime - encryptTime}")
        return String(decrypted)
    }
}