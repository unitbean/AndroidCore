package com.ub.utils.di

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.CreationExtras
import com.ub.utils.ui.biometric.BiometricViewModel
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CoreViewModelProvider @Inject constructor(
    private val context: Context,
    private val dataStore: DataStore<Preferences>
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>, extras: CreationExtras): T {
        return when (modelClass) {
            BiometricViewModel::class.java -> BiometricViewModel(context, dataStore)
            else -> throw IllegalArgumentException("Unknown class $modelClass")
        } as T
    }
}