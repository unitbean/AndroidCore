package com.ub.utils

import androidx.annotation.Keep
import android.util.Log

class LogUtils private constructor() {

    companion object {
        private var consumerThrowable : ConsumerThrowable? = null
        private var consumerString : ConsumerString? = null
        private var isEnableExternalLogging = false
        private var isEnableInternalLogging = true
        private var defaultMessage = "Undefined error"

        @JvmStatic
        fun setThrowableLogger(consumer: ConsumerThrowable?) {
            this.consumerThrowable = consumer
        }

        @JvmStatic
        fun setMessageLogger(consumer: ConsumerString?) {
            this.consumerString = consumer
        }

        @JvmStatic
        fun setDefaultMessage(message: String) {
            this.defaultMessage = message
        }

        @JvmStatic
        fun configure(isEnableExternalLogging: Boolean = false, isEnableInternalLogging: Boolean = true) {
            this.isEnableExternalLogging = isEnableExternalLogging
            this.isEnableInternalLogging = isEnableInternalLogging
        }

        @Keep
        @JvmStatic
        fun e(tag: String, message: String?, throwable: Throwable) {
            if (isEnableInternalLogging) {
                Log.e(tag, message ?: defaultMessage, throwable)
            }
            if (isEnableExternalLogging) {
                consumerThrowable?.consume(tag, throwable)
            }
        }

        @Keep
        @JvmStatic
        fun e(tag: String, message: String?) {
            if (isEnableInternalLogging) {
                Log.e(tag, message ?: defaultMessage)
            }
            if (isEnableExternalLogging) {
                consumerString?.consume(message ?: defaultMessage)
            }
        }

        @Keep
        @JvmStatic
        fun i(tag: String, message: String?) {
            if (isEnableInternalLogging) {
                Log.i(tag, message ?: defaultMessage)
            }
        }

        @Keep
        @JvmStatic
        fun d(tag: String, message: String?) {
            if (isEnableInternalLogging) {
                Log.d(tag, message ?: defaultMessage)
            }
        }

        @Keep
        @JvmStatic
        fun w(tag: String, message: String?) {
            if (isEnableInternalLogging) {
                Log.w(tag, message ?: defaultMessage)
            }
            if (isEnableExternalLogging) {
                consumerString?.consume(message ?: defaultMessage)
            }
        }

        @Keep
        @JvmStatic
        fun w(tag: String, throwable: Throwable) {
            if (isEnableInternalLogging) {
                Log.w(tag, throwable)
            }
            if (isEnableExternalLogging) {
                consumerThrowable?.consume(tag, throwable)
            }
        }

        @Keep
        @JvmStatic
        fun v(tag: String, message: String?) {
            if (isEnableInternalLogging) {
                Log.v(tag, message ?: defaultMessage)
            }
            if (isEnableExternalLogging) {
                consumerString?.consume(message ?: defaultMessage)
            }
        }
    }

    fun interface ConsumerThrowable {
        fun consume(tag: String, throwable: Throwable)
    }

    fun interface ConsumerString {
        fun consume(log: String)
    }
}