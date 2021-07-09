package org.koin.example.test

import org.koin.core.annotation.Module
import org.koin.core.annotation.Single
import org.koin.example.CoffeeMaker
import org.koin.example.Heater
import org.koin.example.Pump
import org.koin.example.Thermosiphon

class TestMe2(val coffeeMaker: CoffeeMaker)


@Module
class ClassModule2 {

    @Single
    fun testMe(coffeeMaker: CoffeeMaker) = TestMe2(coffeeMaker)
}