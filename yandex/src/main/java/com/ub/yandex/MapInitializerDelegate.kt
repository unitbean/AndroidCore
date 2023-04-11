package com.ub.yandex

import androidx.fragment.app.Fragment
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Map initializer delegate
 */
internal fun mapInitializer(mapInitializer: (String) -> Unit) = MapInitializerDelegate(mapInitializer)

internal class MapInitializerDelegate(mapInitializer: (String) -> Unit) : ReadOnlyProperty<Fragment, MapInitializer> {

    private val initializer = MapInitializer(mapInitializer)

    override fun getValue(thisRef: Fragment, property: KProperty<*>): MapInitializer {
        return initializer
    }
}

/**
 * Delegate to set up the YandexApiKey
 *
 * This must be called before using [YandexMapFragment]
 */
interface ApiKeyDelegate {
    fun setApiKey(value: String)
}

internal interface ApiKeyReader {
    fun getApiKey(): String?
}

internal class MapInitializer(private val mapInitializer: (String) -> Unit): ApiKeyDelegate, ApiKeyReader {

    private var apiKey: String? = null

    override fun setApiKey(value: String) {
        this.apiKey = value
    }

    override fun getApiKey(): String? {
        return apiKey
    }

    fun initialize() {
        if (!mapStateInitialization) {
            mapInitializer.invoke(apiKey ?: throw IllegalArgumentException("Please set apk key first"))
            mapStateInitialization = true
        }
    }

    companion object {
        private var mapStateInitialization: Boolean = false
    }
}