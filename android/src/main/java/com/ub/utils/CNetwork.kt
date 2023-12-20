@file:Suppress("UNUSED")

package com.ub.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
import android.net.LinkProperties
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onCompletion
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

@RequiresApi(Build.VERSION_CODES.M)
@Suppress("MissingPermission")
class CNetwork @RequiresPermission(Manifest.permission.ACCESS_NETWORK_STATE) constructor(
    context: Context,
): CoroutineScope by CoroutineScope(Dispatchers.IO) {

    private val manager: ConnectivityManager? = ContextCompat.getSystemService(context, ConnectivityManager::class.java)

    private val connectionState: MutableStateFlow<Map<Network, NetworkSpec>> = MutableStateFlow(
        manager?.activeNetwork?.to(manager.getInternetState())?.let { initialState ->
            mapOf(initialState)
        } ?: mapOf()
    )

    val specFlow: StateFlow<NetworkSpec> = connectionState
        .onEach { delay(AVAILABILITY_LAG) }
        .onEach { println(it); println(manager?.activeNetwork) }
        .map { state -> state[manager?.activeNetwork] ?: NetworkSpec.Disabled }
        .stateIn(
            scope = this,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = connectionState.value.values.firstOrNull() ?: NetworkSpec.Disabled
        )

    val isVpnEnabled: Boolean
        get() = (manager?.getInternetState() as? VpnAware)?.isVpn ?: false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectionState.update { state ->
                if (!state.containsKey(network)) {
                    state + (network to NetworkSpec.Connecting)
                } else state
            }
        }

        override fun onLost(network: Network) {
            connectionState.update { state ->
                state - network
            }
        }

        override fun onCapabilitiesChanged(
            network: Network,
            networkCapabilities: NetworkCapabilities
        ) {
            connectionState.update { state ->
                state + (network to networkCapabilities.getStateByCapabilities())
            }
        }
    }

    init {
        connectionState.run {
            onCompletion {
                manager?.unregisterNetworkCallback(callback)
            }
            launchIn(this@CNetwork)
        }
        val request = NetworkRequest.Builder()
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_NOT_VPN)
            .build()
        manager?.registerNetworkCallback(request, callback)
    }

    private fun ConnectivityManager?.getInternetState(): NetworkSpec {
        val capabilities = this?.getNetworkCapabilities(activeNetwork)
        return capabilities?.getStateByCapabilities() ?: NetworkSpec.Disabled
    }

    private fun NetworkCapabilities.getStateByCapabilities(): NetworkSpec = when {
        hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
            && hasCapability(NetworkCapabilities.NET_CAPABILITY_CAPTIVE_PORTAL) -> {
            NetworkSpec.Captive(isVpn = hasTransport(NetworkCapabilities.TRANSPORT_VPN))
        }
        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
            && hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_CONGESTED)
            && hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_SUSPENDED) -> {
            if (hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
                && hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            ) {
                NetworkSpec.Active(isVpn = hasTransport(NetworkCapabilities.TRANSPORT_VPN))
            } else {
                NetworkSpec.Connecting
            }
        }
        hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            && hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> {
            NetworkSpec.Active(isVpn = hasTransport(NetworkCapabilities.TRANSPORT_VPN))
        }
        hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) -> NetworkSpec.Connecting
        else -> NetworkSpec.Disabled
    }

    companion object {

        /**
         * Искусственная задержка для того, чтобы менеджер успел перестроить данные у себя
         *
         * Иначе в [ConnectivityManager.NetworkCallback.onLost] прилетает null как активный [Network],
         * например, при отключении [NetworkCapabilities.TRANSPORT_WIFI] и переходе на [NetworkCapabilities.TRANSPORT_CELLULAR]
         */
        private const val AVAILABILITY_LAG = 100L
    }
}

sealed class NetworkSpec {
    sealed class Established : NetworkSpec(), VpnAware

    data class Active(
        override val isVpn: Boolean
    ) : Established()

    /**
     * Если попали принудительно на портал регистрации. По факту интернет есть, но использовать его не получается
     */
    data class Captive(
        override val isVpn: Boolean
    ) : Established()

    data object Connecting : NetworkSpec()

    data object Disabled : NetworkSpec()
}

sealed interface VpnAware {
    val isVpn: Boolean
}