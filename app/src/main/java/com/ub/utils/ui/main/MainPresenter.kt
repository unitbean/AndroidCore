package com.ub.utils.ui.main

import android.content.Context
import com.ub.utils.LogUtils
import com.ub.utils.cNetwork
import com.ub.utils.di.services.api.responses.PostResponse
import com.ub.utils.renew
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.functions.Consumer
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import moxy.InjectViewState
import moxy.MvpPresenter
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.CoroutineContext

@InjectViewState
class MainPresenter : MvpPresenter<MainView>(), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Main

    private val interactor = MainInteractor(MainRepository())
    private val list = ArrayList<PostResponse>()
    private val subscriptions = CompositeDisposable()

    override fun onDestroy() {
        subscriptions.clear()
    }

    fun load() {
        val postsTask = interactor.loadPosts()
            .map {
                list.renew(it)
            }
            .subscribeOn(Schedulers.io())
            .delay(100, TimeUnit.MILLISECONDS)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ viewState.done() },
                { LogUtils.e("POST", it.message ?: "Error", it) })
        subscriptions.add(postsTask)
    }

    fun generatePushContent() {
        val pushTask = interactor.generatePushContent(list)
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { viewState.showPush(it) })
        subscriptions.add(pushTask)
    }

    fun networkTest(context: Context) {
        launch {
            try {
                val network = context.cNetwork

                network.startListener().collect {
                    viewState.onConnectivityChange(it)
                }
            } catch (e: Exception) {
                LogUtils.e("NetworkTest", e.message, e)
            }
        }
    }

    fun isEquals() {
        val equalsTask = interactor.isEquals()
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(Consumer { viewState.isEquals(it) })
        subscriptions.add(equalsTask)
    }

    fun loadImage(url: String) {
        launch {
            try {
                val image = interactor.loadImage(url)
                viewState.showImage(image)
            } catch (e: Exception) {
                LogUtils.e("ImageDownload", e.message, e)
            }
        }
    }
}