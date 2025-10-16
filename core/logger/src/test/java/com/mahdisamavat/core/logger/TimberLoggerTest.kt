package com.mahdisamavat.core.logger

import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import timber.log.Timber

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [28])
class TimberLoggerTest {

    private lateinit var logger: Logger

    @Before
    fun setup() {
        Timber.plant(Timber.DebugTree())
        logger = TimberLogger()
    }

    @After
    fun tearDown() {
        Timber.uprootAll()
    }

    @Test
    fun `debug log without throwable works`() {
        logger.d("TestTag", "Debug message")
    }

    @Test
    fun `debug log with throwable works`() {
        val exception = kotlin.RuntimeException("Test exception")
        logger.d("TestTag", "Debug message", exception)
    }

    @Test
    fun `info log without throwable works`() {
        logger.i("TestTag", "Info message")
    }

    @Test
    fun `info log with throwable works`() {
        val exception = kotlin.RuntimeException("Test exception")
        logger.i("TestTag", "Info message", exception)
    }

    @Test
    fun `warning log without throwable works`() {
        logger.w("TestTag", "Warning message")
    }

    @Test
    fun `warning log with throwable works`() {
        val exception = kotlin.RuntimeException("Test exception")
        logger.w("TestTag", "Warning message", exception)
    }

    @Test
    fun `error log without throwable works`() {
        logger.e("TestTag", "Error message")
    }

    @Test
    fun `error log with throwable works`() {
        val exception = kotlin.RuntimeException("Test exception")
        logger.e("TestTag", "Error message", exception)
    }

    @Test
    fun `verbose log without throwable works`() {
        logger.v("TestTag", "Verbose message")
    }

    @Test
    fun `verbose log with throwable works`() {
        val exception = kotlin.RuntimeException("Test exception")
        logger.v("TestTag", "Verbose message", exception)
    }
}
