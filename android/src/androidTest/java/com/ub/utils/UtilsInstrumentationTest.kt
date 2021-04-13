package com.ub.utils

import androidx.test.InstrumentationRegistry
import androidx.test.runner.AndroidJUnit4
import okhttp3.MediaType
import okhttp3.ResponseBody

import org.junit.Test
import org.junit.runner.RunWith

import org.junit.Assert.*
import org.junit.Before
import retrofit2.HttpException
import retrofit2.Response

@RunWith(AndroidJUnit4::class)
class UtilsInstrumentationTest {

    @Before
    fun prepare() {
        LogUtils.configure(true)
        UbUtils.init(InstrumentationRegistry.getTargetContext())
    }

    @Test
    fun testLogUtils() {
        LogUtils.configure(false)
        LogUtils.setThrowableLogger { _, throwable ->
            assertTrue(throwable is HttpException)
        }
        LogUtils.e("TestLogUtils", "http", HttpException(Response.error<String>(401, ResponseBody.create(MediaType.parse("plain/text"), "test error"))))
    }

    @Test
    fun openMarket() {
        assertTrue(openMarket(InstrumentationRegistry.getTargetContext()))
    }

    @Test
    fun validatePhone() {
        assertTrue(isValidPhoneNumber("8 906 169 93 29"))
        assertFalse(isValidPhoneNumber("+ +906 169 93 29"))
    }

    @Test
    fun validateEmails() {
        assertTrue(isValidEmail("unitbean@gmail.com"))
        assertTrue(isValidEmail("unitbean@mail.ru"))
        assertFalse(isValidEmail("unitbean@gmail"))
        assertFalse(isValidEmail("unitbean@gmail."))
        assertFalse(isValidEmail("unitbean2gmail.com"))
        assertTrue(isValidEmail("u@g.com"))
    }
}
