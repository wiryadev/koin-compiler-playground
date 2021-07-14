package org.koin.example.test

import org.koin.core.annotation.*
import org.koin.dsl.module


class TestMe


@Module
class ClassModule3 {

    @Single
    fun testme2() = TestMe()
}