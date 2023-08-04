package com.ub.utils.di.components

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModelProvider
import com.ub.utils.di.CoreViewModelProvider
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@AppScope
@Component
abstract class AppComponent(private val application: Application) {

    abstract val viewModelFactory: ViewModelProvider.Factory

    protected val CoreViewModelProvider.bind: ViewModelProvider.Factory
        @Provides @AppScope get() = this

    val context: Application
        @Provides @AppScope get() = application

    val dataStore: DataStore<Preferences>
        @Provides @AppScope get() = PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("expresspanda")
            }
        )
}

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope