package com.ub.utils.di.components

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.ub.utils.ui.biometric.BiometricViewModel
import kotlinx.serialization.json.Json
import me.tatarka.inject.annotations.Component
import me.tatarka.inject.annotations.Provides
import me.tatarka.inject.annotations.Scope

@AppScope
@Component
abstract class AppComponent(private val application: Application) {

    val viewModelFactory: ViewModelProvider.Factory
        @Provides @AppScope get() = viewModelFactory {
            initializer {
                BiometricViewModel(application, dataStore)
            }
        }

    val context: Application
        @Provides @AppScope get() = application

    val json: Json
        @Provides @AppScope get() = Json

    val dataStore: DataStore<Preferences>
        @Provides @AppScope get() = PreferenceDataStoreFactory.create(
            produceFile = {
                context.preferencesDataStoreFile("androidCore")
            }
        )
}

@Scope
@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION, AnnotationTarget.PROPERTY_GETTER)
annotation class AppScope