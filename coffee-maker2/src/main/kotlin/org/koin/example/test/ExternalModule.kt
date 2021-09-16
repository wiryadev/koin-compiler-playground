package org.koin.example.test

import org.koin.core.annotation.*


class TestComponent


@Module
class ExternalModule {

    @Single(createdAtStart = true)
    @Qualifier("test")
    fun testComponent() = TestComponent()
}