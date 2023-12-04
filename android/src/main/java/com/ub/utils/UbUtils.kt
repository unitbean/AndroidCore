package com.ub.utils

import android.Manifest
import android.app.Activity
import android.content.*
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.util.MalformedJsonException
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import androidx.annotation.RequiresPermission
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.Callback
import okhttp3.MediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.ResponseBody
import okio.Buffer
import okio.BufferedSource
import retrofit2.HttpException
import java.io.IOException
import java.io.InputStream
import java.net.ConnectException
import java.net.InetAddress
import java.net.URL
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.util.*
import java.util.concurrent.TimeoutException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

fun Context.copyTextToClipboard(text: String, label: String = "text"): Boolean {
    val clipManager = ContextCompat.getSystemService(this, ClipboardManager::class.java)
    val clipData = ClipData.newPlainText(label, text)
    return clipManager?.let { manager ->
        manager.setPrimaryClip(clipData)
        true
    } ?: false
}

/**
 * Ленивая последовательность элементов
 * Идеально подходит для таймера
 */
val timer = sequence {
    var cur = 1
    while (true) {
        yield(cur)
        cur += 1
    }
}

/**
 * Сравнение двух коллекций на предмет совпадения содержимого
 * Содержимое может быть не по порядку
 */
fun <T> haveSameElements(col1: Collection<T>?, col2: Collection<T>?): Boolean {
    if (col1 === col2)
        return true

    // If either list is null, return whether the other is empty
    if (col1 == null)
        return col2!!.isEmpty()
    if (col2 == null)
        return col1.isEmpty()

    // If lengths are not equal, they can't possibly match
    if (col1.size != col2.size)
        return false

    // Helper class, so we don't have to do a whole lot of autoboxing
    class Count {
        // Initialize as 1, as we would increment it anyway
        var count = 1
    }

    val counts = HashMap<T, Count>()

    // Count the items in list1
    for (item in col1) {
        val count = counts[item]
        if (count != null)
            count.count++
        else
        // If the map doesn't contain the item, put a new count
            counts[item] = Count()
    }

    // Subtract the count of items in list2
    for (item in col2) {
        val count = counts[item]
        // If the map doesn't contain the item, or the count is already reduced to 0, the lists are unequal
        if (count == null || count.count == 0)
            return false
        count.count--
    }

    // If any count is nonzero at this point, then the two lists don't match
    for (count in counts.values)
        if (count.count != 0)
            return false

    return true
}

/**
 * Проверка на валидность введенного номера
 */
fun isValidPhoneNumber(number: String): Boolean {
    return android.util.Patterns.PHONE.matcher(number).matches()
}

/**
 * Проверка на валидность введенного почтового адреса
 */
fun isValidEmail(email: String): Boolean {
    return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

/**
 * Simple call to get IP address of device
 *
 * Alternate sources:
 *
 * [Herouku](https://api.ipify.org) IPv4
 *
 * [Herouku](https://api64.ipify.org) IPv4/v6
 *
 * [AWS](https://checkip.amazonaws.com)
 *
 * [SeeIP](https://api.seeip.org) IPv4/v6
 *
 * [IpInfo](https://ipinfo.io/ip)
 *
 * [IpEcho](https://ipecho.net/plain)
 *
 * [IfConfig](https://ifconfig.me/ip)
 *
 * [ICanHazIp](http://icanhazip.com)
 *
 * [TrackIp](http://www.trackip.net/ip) IPv4/v6
 *
 * [IPAPI](https://ipapi.co/ip) IPv4/v6
 *
 * @see [java.net.InetAddress] for working with type of address
 */
@RequiresPermission(Manifest.permission.INTERNET)
suspend fun getMyPublicIp(source: String = "https://api64.ipify.org"): Result<InetAddress> =
    withContext(Dispatchers.IO) {
        try {
            val connection = URL(source).openConnection()
            connection.getInputStream().use { iStream ->
                val buff = ByteArray(1024)
                val read = iStream.read(buff)
                Result.success(InetAddress.getByName(String(buff, 0, read)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

/**
 * Определение, является ли ошибка сетевой
 */
val Throwable.isNetworkException: Boolean
    get() = this is ConnectException
        || this is UnknownHostException
        || this is TimeoutException
        || this is MalformedJsonException
        || this is HttpException

/**
 * Safely extract content of [RequestBody]
 *
 * This may be very **hard** operation, please use it carefully
 */
val RequestBody.bodyToString: String?
    get() = try {
        val buffer = Buffer()
        writeTo(buffer)
        buffer.readUtf8()
    } catch (e: IOException) {
        null
    } catch (e: AssertionError) {
        null
    }

/**
 * Safely extract content of [RequestBody]
 *
 * This may be very **hard** operation, please use it carefully
 */
val ResponseBody.bodyToString: String
    get() {
        val source: BufferedSource = source()
        source.request(Long.MAX_VALUE)
        val charset = contentType()?.charset(Charsets.UTF_8) ?: Charsets.UTF_8
        return source.buffer.clone().readString(charset)
    }

/**
 * Check, if device is Samsung for showing DatePicker without bugs
 */
fun isBrokenSamsungDevice(): Boolean {
    return (Build.MANUFACTURER.equals("samsung", ignoreCase = true)
        && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
        && Build.VERSION.SDK_INT <= Build.VERSION_CODES.LOLLIPOP_MR1)
}

/**
 * Прячет клавиатуру
 */
@Deprecated("Please use [Window.keyboardForView]")
fun hideSoftKeyboard(context: Context) {
    try {
        val currentFocus = (context as? Activity)?.currentFocus
        val inputMethodManager = ContextCompat.getSystemService(context, InputMethodManager::class.java)
        inputMethodManager?.hideSoftInputFromWindow(currentFocus!!.windowToken, 0)
        currentFocus?.clearFocus()
    } catch (e: NullPointerException) {
        LogUtils.e("KeyBoard", "NULL point exception in input method service")
    }
}

/**
 * Открывает клавиатуру
 */
@Deprecated("Please use [Window.keyboardForView]")
fun openSoftKeyboard(context: Context, view: View) {
    val inputMethodManager = ContextCompat.getSystemService(context, InputMethodManager::class.java)
    inputMethodManager?.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
}

/**
 * Show or hide keyboard in modern way with handle [androidx.core.graphics.Insets]
 */
fun Window.keyboardForView(view: View, isShow: Boolean) {
    val controller = WindowCompat.getInsetsController(this, view)
    if (isShow && view.isFocusable) {
        controller.show(WindowInsetsCompat.Type.ime())
        view.requestFocus()
    } else if (!isShow && view == currentFocus) {
        controller.hide(WindowInsetsCompat.Type.ime())
        currentFocus?.clearFocus()
    }
}

val Window.isKeyboardIsVisible: Boolean
    get() = currentFocus?.let { focusedView ->
        ViewCompat.getRootWindowInsets(focusedView)?.isVisible(WindowInsetsCompat.Type.ime())
    } ?: false

/**
 * Открывает страницу приложения в Google Play Market
 * @return - успешность операции: true - ссылка открыта, false - не открыта
 */
fun openMarket(context: Context) : Boolean {
    val uri = Uri.parse("market://details?id=" + context.packageName)
    val market = Intent(Intent.ACTION_VIEW, uri)
    return if (market.resolveActivity(context.packageManager) != null) {
        context.startActivity(market)
        true
    } else {
        val browser = Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=${context.packageName}"))
        if (browser.resolveActivity(context.packageManager) != null) {
            context.startActivity(browser)
            true
        } else {
            false
        }
    }
}

/**
 * Create the [Intent] to open location in external map application
 *
 * Recommended use this with wrap in [Intent.createChooser] for better UX
 */
fun createOpenLocationExternalIntent(location: Location, name: String? = null): Intent {
    val geo = buildString {
        append("geo:${location.latitude},${location.longitude}")
        append("?q=${location.latitude},${location.longitude}")
        if (!name.isNullOrEmpty()) {
            append("(${name})")
        }
    }
    val gmmIntentUri: Uri = Uri.parse(geo)
    return Intent(Intent.ACTION_VIEW, gmmIntentUri)
}

/**
 * Определение, включена ли геолокация на устройстве
 */
fun isGpsIsEnabled(context: Context): Boolean {
    val manager = ContextCompat.getSystemService(context, LocationManager::class.java)
    val networkLocationEnabled = manager?.isProviderEnabled(LocationManager.NETWORK_PROVIDER) == true
    val gpsLocationEnabled = manager?.isProviderEnabled(LocationManager.GPS_PROVIDER) == true

    return networkLocationEnabled || gpsLocationEnabled
}

/**
 * Coroutine-cancellable-реализация загрузки объекта из сети
 *
 * Процесс работы:
 * 1. Загрузка по переданному [url] объекта [okhttp3.Response] с помощью [OkHttpClient]
 * 2. Преобразование с помощью [objectMapper] в объект типа [T] с учетом возможного пустого ответа
 * 3. Отдача в существующую корутину результата операции
 *
 * В случае отмены операции с помощью [okhttp3.Call.cancel] сетевой запрос отменяется, если возможно
 */
suspend inline fun <T> OkHttpClient.download(url: String, crossinline objectMapper: (byteStream: InputStream?) -> T?) =
    suspendCancellableCoroutine { continuation ->
        val request = Request.Builder().url(url).build()
        val call = newCall(request)
        call.enqueue(object : Callback {
            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                if (!continuation.isCompleted) {
                    try {
                        val result = objectMapper.invoke(response.body()?.byteStream())
                        continuation.resume(result)
                    } catch (e: Exception) {
                        continuation.resumeWithException(e)
                    }
                }
            }

            override fun onFailure(call: okhttp3.Call, e: IOException) {
                if (!continuation.isCompleted) {
                    continuation.resumeWithException(e)
                }
            }
        })

        continuation.invokeOnCancellation {
            call.cancel()
        }
    }

/**
 * Wrapper of token-update logic.
 * In case of receiving [HttpException] with code 401 during execution of [action],
 * this will try to refresh access-token with [updateToken]. If in process of updating access-token you also will receive [HttpException] with code 403,
 * you must execute the [logout] action to de-authenticate user
 *
 * In cases of another types of error, they will be transmitted directly to callsite of this function, and you will be should handled they manually
 *
 * For resolving concurrency problem with parallel requests there are a one of possible decisions:
 * you should use shared [kotlinx.coroutines.Deferred] instance logic for parameter [updateToken]
 * and desirable for [afterUpdate] for a consistent record of changed values. This shared instance should be used by all parallel request,
 * and after executing of all logic this instance should be cleared with setting value to null
 *
 * @param updateToken - action with updating access-token instance. This returns value [U], that will be transmitted to arguments of [afterUpdate] function
 * @param refreshToken - string value of refresh-token to update access-token
 * @param afterUpdate - action that must be done after updating access-token. In most cases that can be include saving new value of access-token
 * @param logout - action that must be execute on failure of updating access-token
 * @param action - action that desired to execute with update access-token wrapped logic. This also return result value of this function [T]
 */
@Deprecated("Please use [okhttp3.Authenticator] for refreshing token")
suspend inline fun <T, U> retryWithRefreshToken(
    crossinline updateToken: suspend (String) -> U,
    refreshToken: String,
    crossinline afterUpdate: suspend (U) -> Unit,
    crossinline logout: suspend () -> Unit,
    crossinline action: suspend () -> T
): T {
    return try {
        action.invoke()
    } catch (e: Exception) {
        if (e is HttpException && e.code() == 401) {
            val tokenUpdateResponse = try {
                updateToken.invoke(refreshToken)
            } catch (e: Exception) {
                if (e is HttpException && e.code() == 403) {
                    logout.invoke()
                }
                throw e
            }
            afterUpdate.invoke(tokenUpdateResponse)
            action.invoke()
        } else throw e
    }
}