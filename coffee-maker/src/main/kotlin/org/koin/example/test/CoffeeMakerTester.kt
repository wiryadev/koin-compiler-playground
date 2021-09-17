package org.koin.example.test

import org.koin.core.annotation.Factory
import org.koin.core.annotation.Qualifier
import org.koin.core.annotation.Single
import org.koin.example.coffee.CoffeeMaker

@Factory
@Qualifier("test")
class CoffeeMakerTester(val coffeeMaker: CoffeeMaker)

interface CoffeeMakerTesterTest {
    fun coffeeTest()
}

class CoffeeMakerTesterTestImpl(val coffeeMaker: CoffeeMaker) : CoffeeMakerTesterTest {
    override fun coffeeTest() {
        coffeeMaker.brew()
    }
}
