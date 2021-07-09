package org.koin.example.test

import org.koin.core.annotation.*
import org.koin.dsl.module


class TestMe

//@ComponentScan
//val testModule = module {
//    useGeneratedComponents()
//}

@Module
class ClassModule3 {

    @Single
    fun testme2() = TestMe()
}