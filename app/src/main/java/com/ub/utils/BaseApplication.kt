package com.ub.utils

import android.app.Application
import com.ub.utils.di.components.AppComponent
import com.ub.utils.di.components.DaggerAppComponent
import com.ub.utils.di.components.DaggerMainComponent
import com.ub.utils.di.components.MainComponent
import com.ub.utils.di.modules.MainModule

class BaseApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        initDI()
    }

    private fun initDI() {
        appComponent = DaggerAppComponent.builder()
            .context(this)
            .build()
    }

    companion object {

        lateinit var appComponent: AppComponent
            private set

        fun createMainComponent(): MainComponent {
            return DaggerMainComponent.builder()
                .appComponent(appComponent)
                .build()
        }
    }
}