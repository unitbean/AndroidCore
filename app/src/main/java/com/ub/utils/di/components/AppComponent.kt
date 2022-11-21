package com.ub.utils.di.components

import android.content.Context
import com.ub.utils.di.CoreViewModelProvider
import com.ub.utils.di.modules.CoreModule
import com.ub.utils.di.services.ApiService
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(modules = [CoreModule::class])
interface AppComponent {

    val apiService: ApiService

    val viewModelProvider: CoreViewModelProvider

    @Component.Builder
    interface Builder {

        @BindsInstance
        fun context(context: Context): Builder

        fun build(): AppComponent
    }
}