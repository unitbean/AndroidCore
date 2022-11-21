package com.ub.utils.di.components

import com.ub.utils.di.modules.MainModule
import com.ub.utils.di.modules.MainScope
import com.ub.utils.ui.main.MainViewModelProvider
import dagger.Component

@MainScope
@Component(
    modules = [MainModule::class],
    dependencies = [AppComponent::class]
)
interface MainComponent {
    val provider: MainViewModelProvider
}