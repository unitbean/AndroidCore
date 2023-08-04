package com.ub.utils

import android.app.Application
import com.ub.utils.di.components.AppComponent
import com.ub.utils.di.components.MainComponent
import com.ub.utils.di.components.create
import timber.log.Timber

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initDI()

        Timber.plant(Timber.DebugTree())
    }

    private fun initDI() {
        appComponent = AppComponent::class.create(this)
    }

    companion object {

        lateinit var appComponent: AppComponent
            private set

        fun createMainComponent(): MainComponent {
            return MainComponent::class.create(appComponent)
        }
    }
}