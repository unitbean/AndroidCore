@file:Suppress("UNUSED")

package com.ub.utils

import android.Manifest
import android.content.Context
import android.net.ConnectivityManager
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

    private val connectionState: MutableStateFlow<Map<Network, LocalNetwork>> = MutableStateFlow(
        manager?.activeNetwork?.to(manager.getInternetState())?.let { initialState ->
            mapOf(initialState)
        } ?: mapOf()
    )

    val specFlow: StateFlow<NetworkSpec> = connectionState
        .onEach { delay(AVAILABILITY_LAG) }
        .map { state ->
            state[manager?.activeNetwork]?.takeIf {
                it.isFactTransport || state.values.any(LocalNetwork::isFactTransport)
            }?.spec ?: NetworkSpec.Disabled
        }
        .stateIn(
            scope = this,
            started = SharingStarted.WhileSubscribed(5000L),
            initialValue = connectionState.value.values.firstOrNull()?.takeIf(LocalNetwork::isFactTransport)?.spec ?: NetworkSpec.Disabled
        )

    val isVpnEnabled: Boolean
        get() = (manager?.getInternetState()?.spec as? VpnAware)?.isVpn ?: false

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            connectionState.update { state ->
                if (!state.containsKey(network)) {
                    state + (network to LocalNetwork(isFactTransport = false, spec = NetworkSpec.Connecting))
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
                state + (network to LocalNetwork(networkCapabilities))
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

    private fun ConnectivityManager?.getInternetState(): LocalNetwork {
        val capabilities = this?.getNetworkCapabilities(activeNetwork)
        return LocalNetwork(capabilities)
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

@RequiresApi(Build.VERSION_CODES.M)
private fun NetworkCapabilities.isFactTransport(): Boolean {
    if (hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
        || hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
        || hasTransport(NetworkCapabilities.TRANSPORT_BLUETOOTH)
        || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && hasTransport(NetworkCapabilities.TRANSPORT_WIFI_AWARE)) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1 && hasTransport(NetworkCapabilities.TRANSPORT_LOWPAN)) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && hasTransport(NetworkCapabilities.TRANSPORT_USB)) return true
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE && hasTransport(NetworkCapabilities.TRANSPORT_THREAD)) return true
    return false
}

@RequiresApi(Build.VERSION_CODES.M)
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

/**
 * @param isFactTransport нужен для определения, не является ли данная сеть VPN only. Такое наблюдается на устройствах в районе SDK~26
 */
private data class LocalNetwork(
    val isFactTransport: Boolean,
    val spec: NetworkSpec
) {
    @RequiresApi(Build.VERSION_CODES.M)
    constructor(capabilities: NetworkCapabilities?): this(
        isFactTransport = capabilities?.isFactTransport() ?: false,
        spec = capabilities?.getStateByCapabilities() ?: NetworkSpec.Disabled
    )
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