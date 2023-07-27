package com.ub.utils.di.components

import com.ub.utils.ui.main.IMainRepository
import com.ub.utils.ui.main.MainInteractor
import com.ub.utils.ui.main.MainRepository
import com.ub.utils.ui.main.MainViewModel
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@MainScope
@Component
abstract class MainComponent(@Component val parent: AppComponent) {

    abstract val interactor: MainInteractor

    protected val MainRepository.bind: IMainRepository
        @Provides @MainScope get() = this

    abstract fun mainViewModelFactory(): (urlToLoad: String) -> MainViewModel
}

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class MainScope