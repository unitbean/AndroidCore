package com.ub.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType
import okhttp3.ResponseBody
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response

class RetryRefreshTest {

    private val accessTokenFailError: Exception = HttpException(
        Response.error<String>(
            401,
            ResponseBody.create(
                MediaType.parse(
                    "application/json"
                ),
                "{\"error\":false}"
            )
        )
    )
    private val refreshTokenFailError: Exception = HttpException(
        Response.error<String>(
            403,
            ResponseBody.create(
                MediaType.parse(
                    "application/json"
                ),
                "{\"error\":true}"
            )
        )
    )
    private var tokenUpdateTask: Deferred<String>? = null
    @OptIn(DelicateCoroutinesApi::class)
    private val updateToken: suspend (String) -> String = { refreshToken ->
        if (refreshToken != REFRESH_TOKEN) throw refreshTokenFailError
        if (tokenUpdateTask?.isActive != true) {
            tokenUpdateTask = GlobalScope.async {
                delay(200)
                return@async VALID_ACCESS_TOKEN
            }
        }

        tokenUpdateTask?.await() ?: throw NullPointerException("Update task is null")
    }
    private val afterUpdate: suspend (String) -> Unit = { updateResult ->
        if (updateResult == VALID_ACCESS_TOKEN) {
            tokenUpdateTask = null
        }
    }
    private val logout: suspend () -> Unit = {
        throw IllegalAccessException("Logout has completed")
    }

    @Test
    fun updateTokenSuccessTest() {
        var token = INVALID_ACCESS_TOKEN
        runBlocking {
            val result = retryWithRefreshToken(
                updateToken = updateToken,
                refreshToken = REFRESH_TOKEN,
                afterUpdate = { updateResult ->
                    if (updateResult == VALID_ACCESS_TOKEN) {
                        tokenUpdateTask = null
                        token = VALID_ACCESS_TOKEN
                    }
                },
                logout = logout,
                action = {
                    if (token != VALID_ACCESS_TOKEN) {
                        throw accessTokenFailError
                    } else "Ok"
                }
            )
            assertTrue(result == "Ok")
        }
    }

    @Test(expected = IllegalAccessException::class)
    fun updateTokenFailTest() {
        runBlocking {
            retryWithRefreshToken(
                updateToken = updateToken,
                refreshToken = INVALID_REFRESH_TOKEN,
                afterUpdate = afterUpdate,
                logout = logout,
                action = {
                    throw accessTokenFailError
                }
            )
        }
    }

    @Test(timeout = 300L)
    fun updateParallelTokenSuccessTest() {
        var token = INVALID_ACCESS_TOKEN
        runBlocking {
            val resultFirst = async {
                retryWithRefreshToken(
                    updateToken = updateToken,
                    refreshToken = REFRESH_TOKEN,
                    afterUpdate = { updateResult ->
                        if (updateResult == VALID_ACCESS_TOKEN) {
                            tokenUpdateTask = null
                            token = VALID_ACCESS_TOKEN
                        }
                    },
                    logout = logout,
                    action = {
                        if (token != VALID_ACCESS_TOKEN) {
                            throw accessTokenFailError
                        } else "Ok"
                    }
                )
            }
            val resultSecond = async {
                retryWithRefreshToken(
                    updateToken = updateToken,
                    refreshToken = REFRESH_TOKEN,
                    afterUpdate = { updateResult ->
                        if (updateResult == VALID_ACCESS_TOKEN) {
                            tokenUpdateTask = null
                            token = VALID_ACCESS_TOKEN
                        }
                    },
                    logout = logout,
                    action = {
                        if (token != VALID_ACCESS_TOKEN) {
                            throw accessTokenFailError
                        } else "Ok"
                    }
                )
            }

            assertTrue(resultFirst.await() == "Ok")
            assertTrue(resultSecond.await() == "Ok")
        }
    }

    companion object {
        private const val INVALID_ACCESS_TOKEN = "invalid_token"
        private const val VALID_ACCESS_TOKEN = "valid_token"
        private const val REFRESH_TOKEN = "refresh_token"
        private const val INVALID_REFRESH_TOKEN = "error"
    }
}