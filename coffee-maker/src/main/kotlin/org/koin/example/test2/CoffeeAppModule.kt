package org.koin.example.test2

import org.koin.core.annotation.*
import org.koin.dsl.module
import org.koin.example.*
import org.koin.example.test.TestMe
import kotlin.reflect.KClass


@Module
class ClassModule {

    @Single
    fun electricHeater() : Heater = ElectricHeater()

    @Single
    fun thermosiphon(heater: Heater) : Pump = Thermosiphon(heater)

    @Single
    fun coffeeMaker(heater: Heater, pump : Pump) = CoffeeMaker(pump,heater)
}

