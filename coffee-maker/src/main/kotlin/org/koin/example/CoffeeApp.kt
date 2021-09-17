package org.koin.example

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.startKoin
import org.koin.core.qualifier.StringQualifier
import org.koin.core.time.measureDuration
import org.koin.example.coffee.CoffeeMaker
import org.koin.example.di.CoffeeAppModule
import org.koin.example.di.CoffeeTesterModule
import org.koin.example.tea.TeaModule
import org.koin.example.tea.TeaPot
import org.koin.example.test.CoffeeMakerTester
import org.koin.example.test.CoffeeMakerTesterTest
import org.koin.example.test.ExternalModule
import org.koin.example.test.TestComponent
import org.koin.ksp.generated.*
import org.koin.mp.KoinPlatformTools

class CoffeeApp : KoinComponent {
    val maker: CoffeeMaker by inject()
}

// be sure to import "import org.koin.ksp.generated.*"

fun main() {
    startKoin {
        // if no module
//        defaultModule()

        // else let's use our modules
        modules(
            CoffeeAppModule().module,
            CoffeeTesterModule().module,
            TeaModule().module,
            ExternalModule().module,
        )
    }

    val coffeeShop = CoffeeApp()
    measureDuration("Got Coffee") {
        coffeeShop.maker.brew()
    }

    // Tests
    val koin = KoinPlatformTools.defaultContext().get()
    koin.get<TeaPot>().heater
    koin.get<CoffeeMakerTester>(StringQualifier("test"))
    koin.get<CoffeeMakerTesterTest>().coffeeTest()
    koin.get<TestComponent>(StringQualifier("test"))
}

fun measureDuration(msg: String, code: () -> Unit): Double {
    val duration = measureDuration(code)
    println("$msg in $duration ms")
    return duration
}