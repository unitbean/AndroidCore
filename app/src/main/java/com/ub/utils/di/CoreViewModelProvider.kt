package com.ub.utils.di

import android.app.Application
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ub.utils.di.components.AppScope
import com.ub.utils.ui.biometric.BiometricViewModel
import me.tatarka.inject.annotations.Inject

@AppScope
@Inject
class CoreViewModelProvider(
    private val application: Application,
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            BiometricViewModel::class.java -> BiometricViewModel(application, dataStore)
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}