package org.koin.example.test2

import org.koin.core.annotation.*
import org.koin.dsl.module
import org.koin.example.Heater
import org.koin.example.Pump
import org.koin.example.Thermosiphon
import org.koin.example.test.TestMe
import kotlin.reflect.KClass


@Module
class ClassModule {

    @Single
    fun pump(heater: Heater) : Pump = Thermosiphon(heater)
}

