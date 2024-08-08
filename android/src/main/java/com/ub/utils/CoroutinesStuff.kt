package com.ub.utils

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestDispatcher
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.coroutines.ContinuationInterceptor
import kotlin.coroutines.coroutineContext
import kotlin.system.measureTimeMillis
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.TimedValue

@OptIn(ExperimentalContracts::class, ExperimentalCoroutinesApi::class)
suspend fun measureCoroutineDuration(
    body: suspend () -> Unit
): Duration {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }
    val dispatcher = coroutineContext[ContinuationInterceptor]
    return if (dispatcher is TestDispatcher) {
        val before = dispatcher.scheduler.currentTime
        body()
        val after = dispatcher.scheduler.currentTime
        after - before
    } else {
        measureTimeMillis {
            body()
        }
    }.milliseconds
}

@OptIn(ExperimentalContracts::class)
suspend fun <T> measureCoroutineTimedValue(
    body: suspend () -> T
): TimedValue<T> {
    contract {
        callsInPlace(body, InvocationKind.EXACTLY_ONCE)
    }
    var value: T
    val duration = measureCoroutineDuration {
        value = body()
    }
    return TimedValue(value, duration)
}